package dev.practice.webclient.timeoutclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class WebTimeoutClientConfig {

    /**
     * 아래는 HttpClient 수준의 전역 timeout 설정을 다룬다. (WebClient 별로 다양한 타임아웃 설정)
     * https://www.baeldung.com/spring-webflux-timeout
     *
     * 참고
     * 아래 예시 외에 SSL/TLS timeout, proxy timeout 도 존재한다.
     *
     * 참고.
     * 아래의 각 timeout 은 서로 중복되는 시점이 없다. (측정되는 시간 순으로 나열, 바로 바로 이어짐)
     * 1. connection time - 연결 단계
     * 2. write time - 요청 데이터 전체를 소켓의 출력 버퍼에 적재 완료(전송 완료)하는데 까지 시간
     * 3. response time - 전송 완료한 시점 부터 응답 데이터 첫 바이트가 도착하는데 까지 시간
     * 4. read time - 응답 데이터의 첫 바이트를 읽는 시점부터 마지막 바이트 까지 읽는시점 까지 시간
     */

    @Bean
    public WebClient httpClientResponseTimoutClient() {

        /**
         * responseTimeout
         * - 요청이 전송되고 최초의 응답(첫 바이트)이 도착하기 까지의 대기 시간
         *
         * ReactorClientHttpConnector 는 WebClient ClientConnector 의 default 구현체이다.
         * HttpClient 를 이용하여 responseTimeout 을 설정해 줄 수 있다.
         *
         * 참고
         * Netty 의 HttpClient 기본 설정은 responseTimeout 설정을 하지 않는다.
         */

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(1L)); // 1초 타임 아웃

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient httpClientConnectionTimeoutClient() {

        /**
         * connectionTimeout
         * - 연결 단계의 시간
         * - 클라이언트와 서버가 연결이 맺어져야되는 시간 (TCP 3-way handshaking)
         * - 지정된 시간 내에 연결이 되지 않거나 끊어지면 io.netty.channel 의 ConnectTimeoutException 이 발생한다.
         *
         * 참고
         * Netty 의 HttpClient 기본 설정은 30초 이다.
         *
         * 참고
         * 연결이 유휴할 때, TCP 확인 프로브를 보내는 연결 유지 옵션 관련은 아래와 같다.
         * ChannelOption.SO_KEEPALIVE
         * EpollChannelOption.TCP_KEEPIDLE
         * EpollChannelOption.TCP_KEEPINTVL
         * EpollChannelOption.TCP_KEEPCNT
         */

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000); // 10초 타임 아웃

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient httpClientReadAndWriteTimeoutClient() {

        /**
         * readTimeout
         * - 응답 단계의 시간
         * - 클라이언트가 서버로부터 데이터의 첫 바이트를 받기 시작한 시점부터 모든 데이터를 받을 때까지의 시간
         * - 일정 시간 내에 데이터를 읽지 못한 경우 readTimeout 발생
         * - io.netty.handler.timeout 의 ReadTimeoutException 발생
         *
         * writeTimeout
         * - 요청 단계의 시간
         * - 전송할 데이터의 첫 바이트를 애플리케이션에서 소켓의 출력 버퍼를 통해 전송하기 시작한 시점부터
         * 마지막 바이트를 전송 시도를 완료한 시점까지의 최대 허용 시간을 의미
         * - 쓰기 작업이 특정 시간내에 완료되지 못한 경우 writeTimeout 발생
         * - io.netty.handler.timeout 의 WriteTimeoutException 발생
         *
         * HttpClient 의 doOnConnected 메서드를 통해 콜백으로 설정을 할 수 있다.
         */

        HttpClient httpClient = HttpClient.create()

                .doOnConnected(
                        conn -> conn
                                .addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS)) // in bound -> addHandlerLast
                                .addHandlerFirst(new WriteTimeoutHandler(10) // out bound -> addHandlerFirst
                                )
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
