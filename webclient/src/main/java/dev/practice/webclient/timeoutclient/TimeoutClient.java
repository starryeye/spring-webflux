package dev.practice.webclient.timeoutclient;

import dev.practice.webclient.response.ExchangeResponse;
import io.netty.handler.ssl.SslHandshakeTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

import java.net.http.HttpTimeoutException;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class TimeoutClient {

    private final WebClient helloClient; // hello
    private final WebClient worldClient; // world


    /**
     * 아래는 요청별로 타임 아웃 설정을 다룬다.
     * https://www.baeldung.com/spring-webflux-timeout
     */

    public Mono<ExchangeResponse> responseTimeoutCall() {

        /**
         * 아래 요청에 대해 responseTimeout 을 설정한다.
         *
         * 요청에 대해 설정한 것은..
         * HttpClient 수준의 설정을 재설정하는 격이다. (우선 순위가 가장 높음)
         * null 로 설정하면 responseTimeout default 설정으로 만듬 (default 값.. 원래 응답 타임아웃이 없음)
         */

        return worldClient.get()
                .uri("/v6/latest")
                .httpRequest(
                        clientHttpRequest -> {
                            HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                            reactorRequest.responseTimeout(Duration.ofSeconds(2L)); // 2초 타임아웃
                        }
                )
                .retrieve()
                .bodyToMono(ExchangeResponse.class);
    }

    public Mono<ExchangeResponse> responseTimeoutCall2() {

        /**
         * Reactor core 에서 제공하는 timeout 연산자를 사용한다.
         *
         * java.util.concurrent 의 TimeoutException 이 발생한다.
         */

        return worldClient.get()
                .uri("/v6/latest")
                .retrieve()
                .bodyToMono(ExchangeResponse.class)
                .timeout(Duration.ofSeconds(2L)); // 2초 타임아웃
    }

    /**
     * time out 설정을 배웠으니.. exception 을 핸들링해보자.
     */
    public Mono<ExchangeResponse> exceptionHandlingCall() {

        /**
         * reactor core 의 예외 처리 연산자를 사용해주면 된다.
         */

        return worldClient.get()
                .uri("/v6/latest")
                .retrieve()
                .bodyToMono(ExchangeResponse.class)
                .timeout(Duration.ofSeconds(2L))
                .onErrorMap( // 다른 exception 으로 교체
                        ReadTimeoutException.class,
                        ex -> new HttpTimeoutException("ReadTimeout")
                )
                .onErrorReturn( // 기본 응답 값으로 교체
                        SslHandshakeTimeoutException.class,
                        ExchangeResponse.builder().build()
                )
                .doOnError( // 다른 exception 으로 교체
                        WriteTimeoutException.class,
                        ex -> log.error("WriteTimeout")
                );
    }
}
