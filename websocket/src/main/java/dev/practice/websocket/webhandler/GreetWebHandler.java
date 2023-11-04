package dev.practice.websocket.webhandler;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class GreetWebHandler implements WebHandler {

    // WebHandler 구현
    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {

        // 요청 쿼리 파라미터
        String name = exchange.getRequest().getQueryParams()
                .getFirst("name");

        if(Objects.isNull(name)) {
            name = "world";
        }

        // 응답
        return exchange.getResponse()
                .writeWith(
                        Mono.just(
                                exchange.getResponse()
                                        .bufferFactory().wrap(
                                                ("Hello " + name).getBytes(StandardCharsets.UTF_8)
                                        )
                        )
                );
    }
}
