package dev.practice.webhandler.withwebfilter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

@Slf4j
public class SimpleWebHandler implements WebHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {

        log.info("web handler");

        final ServerHttpResponse response = exchange.getResponse();

        String name = exchange.getAttribute("name"); // filter 에서 넣은 attribute read

        // 응답 값
        String content = "Hello " + name;
        Mono<DataBuffer> responseBody = Mono.just(
                response.bufferFactory().wrap(content.getBytes())
        );

        response.getHeaders()
                .add("Content-Type", "text/plain");

        return response.writeWith(responseBody);
    }
}
