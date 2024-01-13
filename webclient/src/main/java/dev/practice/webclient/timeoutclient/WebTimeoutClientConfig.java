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
     */

    @Bean
    public WebClient httpClientResponseTimoutClient() {

        /**
         * responseTimeout
         * - 요청을 보낸 후, 응답을 받기 까지 대기 시간 설정
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
         * - 클라이언트와 서버가 연결이 맺어져야되는 시간
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
         * - 일정 시간 동안 데이터를 읽지 못한 경우 readTimeout 발생
         * - io.netty.handler.timeout 의 ReadTimeoutException 발생
         *
         * writeTimeout
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
