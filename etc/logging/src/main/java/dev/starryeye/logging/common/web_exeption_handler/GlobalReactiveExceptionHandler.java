package dev.starryeye.logging.common.web_exeption_handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Component
public class GlobalReactiveExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        HttpHeaders headers = exchange.getRequest().getHeaders();

        if (ex instanceof IOException || ex.getClass().getSimpleName().contains("Aborted")) {
            log.error("[Client aborted connection no need to respond] : [{} {}] - {}", method, path, headers, ex);
            return Mono.empty();
        }

        // 예상치 못한 예외
        log.error("[Unhandled exception] at [{} {}] - {}", method, path, headers, ex);

        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return exchange.getResponse().setComplete();
    }
}
