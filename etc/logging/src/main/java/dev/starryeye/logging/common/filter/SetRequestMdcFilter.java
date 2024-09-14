package dev.starryeye.logging.common.filter;

import dev.starryeye.logging.common.ContextMdc;
import dev.starryeye.logging.common.ContextMdcKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class SetRequestMdcFilter implements WebFilter {

    private final ContextMdc contextMdc;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        return chain.filter(exchange)
                .contextWrite(context -> contextMdc.createContext(ContextMdcKey.REQUEST_ID, genRequestId()));
    }

    private String genRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
