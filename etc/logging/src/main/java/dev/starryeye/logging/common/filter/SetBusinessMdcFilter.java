package dev.starryeye.logging.common.filter;

import dev.starryeye.logging.common.ContextMdc;
import dev.starryeye.logging.common.ContextMdcKey;
import dev.starryeye.logging.common.LoggingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Order(3)
@Component
@RequiredArgsConstructor
public class SetBusinessMdcFilter implements WebFilter {

    private final ContextMdc contextMdc;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.fromRunnable(() ->
                        contextMdc.put(ContextMdcKey.LOGGING_TYPE, LoggingType.BUSINESS.name()))
                .onErrorResume(throwable -> {
                    log.error("Failed to put context MDC", throwable);
                    return Mono.empty();
                })
                .then(chain.filter(exchange));
    }
}
