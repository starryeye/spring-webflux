package dev.starryeye.logging.common.filter;

import dev.starryeye.logging.common.ContextMdc;
import dev.starryeye.logging.common.ContextMdcKey;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Order(4)
@Component
@RequiredArgsConstructor
public class SetResponseMdcFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String requestId = ContextMdc.get(ContextMdcKey.REQUEST_ID);

        ServerHttpResponse response = exchange.getResponse();

        response.beforeCommit(() -> {
            response.getHeaders().put("X-Request-ID", Collections.singletonList(requestId));
            return Mono.empty();
        });

        return chain.filter(exchange);
    }
}
