package dev.practice.webhandler.withcodec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

@Slf4j
public class WebHandlerWithCodec implements WebHandler {

    /**
     * TODO 사용된 객체의 책임을 알아볼것..
     * - CodecConfigurer
     * - ServerRequest
     * - Post 에 Content-Type: application/json 이 없거나 GET 으로 하면 응답 Publisher 가 동작하지 않음
     */

    // CodecConfigurer 생성
    private final CodecConfigurer codecConfigurer = ServerCodecConfigurer.create();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {

        log.info("WebHandler");

        // exchange 와 codecConfigurer 로 ServerRequest 생성
        final ServerRequest request = ServerRequest.create(
                exchange,
                codecConfigurer.getReaders()
        );

        final ServerHttpResponse response = exchange.getResponse();

        // ServerRequest::bodyToMono 로 json(요청 바디) 을 객체로 변경
        Mono<NameHolder> bodyMono = request.bodyToMono(NameHolder.class);

        return bodyMono.flatMap(nameHolder -> {

            log.info("Mono");

            String nameQuery = nameHolder.getName();
            String name = nameQuery == null ? "world" : nameQuery;

            String content = "Hello " + name;
            log.info("responseBody: {}", content);
            Mono<DataBuffer> responseBody = Mono.just(
                    response.bufferFactory()
                            .wrap(content.getBytes())
            );

            response.getHeaders()
                    .add("Content-Type", "text/plain");

            return response.writeWith(responseBody);
        });
    }
}
