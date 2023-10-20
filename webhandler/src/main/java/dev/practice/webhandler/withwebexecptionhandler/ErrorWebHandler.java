package dev.practice.webhandler.withwebexecptionhandler;

import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

public class ErrorWebHandler implements WebHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {

        final ServerHttpResponse response = exchange.getResponse();

        return response.writeWith(
                Mono.create(sink -> {

                            // publisher 에서 예외 발생, onError 이벤트 발생
                            sink.error(new CustomException("publisher 에서 custom exception 발생"));

//                            throw new CustomException("test");
                        }
                ));
    }
}
