package dev.practice.webhandler.withheader;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

public class WebHandlerWithHeader implements WebHandler {

    /**
     * WebHandler 는 함수형 인터페이스 이다.
     */

    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {

        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();

        // 요청 헤더 read
        String name = request.getHeaders()
                .getFirst("X-Custom-Name");
        if (name == null) {
            response.setStatusCode(HttpStatus.BAD_REQUEST); // HttpStatus 설정
            return response.setComplete(); // 응답 값
        }

        // 응답 값
        String content = "Hello " + name;
        Mono<DataBuffer> responseBody = Mono.just(
                response.bufferFactory().wrap(content.getBytes())
        );

        // 응답 헤더 설정
        response.getHeaders()
                .add("Content-Type", "text/plain");

        return response.writeWith(responseBody);
    }
}
