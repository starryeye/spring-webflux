package dev.practice.webhandler.withqueryparam;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

@Slf4j
public class WebHandlerWithQueryParameter implements WebHandler {

    /**
     * WebHandler 는 함수형 인터페이스 이다.
     */

    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {

        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();

        String nameQuery = request.getQueryParams().getFirst("name");
        String name = nameQuery == null ? "world" : nameQuery;

        String content = "Hello " + name;
        log.info("responseBody: {}", content);

        // 응답 값
        Mono<DataBuffer> responseBody = Mono.just(
                response.bufferFactory()
                        .wrap(content.getBytes())
        );

        response.addCookie(
                ResponseCookie.from("name", name).build());
        response.getHeaders()
                .add("Content-Type", "text/plain");

        return response.writeWith(responseBody);
    }
}
