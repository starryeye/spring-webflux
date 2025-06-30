package dev.starryeye.logging.common.filter;

import dev.starryeye.logging.common.ContextMdc;
import dev.starryeye.logging.common.ContextMdcKey;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Order(0)
@Component
@RequiredArgsConstructor
public class SetRequestMdcFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        return chain.filter(exchange)
                .contextWrite(ContextMdc.createContext(getContextKeyValueMap(exchange)));
    }

    private Map<ContextMdcKey, String> getContextKeyValueMap(ServerWebExchange exchange) {
        String requestId = genRequestId();

        return createContextKeyValueMap(exchange, requestId);
    }

    private Map<ContextMdcKey, String> createContextKeyValueMap(ServerWebExchange exchange, String requestId) {

        Map<ContextMdcKey, String> contextKeyValueMap = new EnumMap<>(ContextMdcKey.class);
        String path = exchange.getRequest().getPath().value();
        contextKeyValueMap.put(ContextMdcKey.REQUEST_PATH, path);


        HttpHeaders headers = exchange.getRequest().getHeaders();

        String requestIdOfHeader = headers.getFirst("X-Request-ID");
        if (StringUtils.isNotBlank(requestIdOfHeader)) {
            requestId = requestIdOfHeader;
        }

        contextKeyValueMap.put(ContextMdcKey.REQUEST_ID, requestId);

        return contextKeyValueMap;
    }


    private String genRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
