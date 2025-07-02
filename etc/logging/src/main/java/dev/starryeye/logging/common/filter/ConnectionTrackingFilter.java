package dev.starryeye.logging.common.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class ConnectionTrackingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        HttpHeaders headers = exchange.getRequest().getHeaders();

        return chain.filter(exchange)
                .doFinally(signal -> {

                    switch (signal) {
                        case CANCEL -> log.info("[Client cancelled request] [{} {}] - {}", method, path, headers);
                        case ON_ERROR -> log.error("[Request failed] [{} {}] - {}", method, path, headers);
                        default -> {
                            // skip
                        }
                    }
                });
    }
}
