package dev.starryeye.logging.config;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Map;
import java.util.UUID;

@Component
public class MdcLoggingFilter implements WebFilter {

    private static final String MDC_KEY = "mdc";

    @PostConstruct
    public void init() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                MDC_KEY,
                MDC::getCopyOfContextMap,
                MDC::setContextMap,
                MDC::clear
        );
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .contextWrite(Context.of(MDC_KEY, Map.of("requestId", generateRequestId())));
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
