package dev.starryeye.logging.common.filter;

import dev.starryeye.logging.common.ContextMdcKey;
import dev.starryeye.logging.common.ContextMdcValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

import static net.logstash.logback.marker.Markers.append;

@Slf4j
@Order(2)
@Component
public class PrintRequestMdcLogFilter implements WebFilter {

    private static final List<String> PATH_PREFIXES = List.of("/articles");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        RequestPath path = exchange.getRequest().getPath();

        return Mono.just(path)
                .filter(p -> PATH_PREFIXES.stream()
                        .anyMatch(prefix -> p.pathWithinApplication().value().startsWith(prefix)))
                .doOnNext(p -> log.info(append(ContextMdcKey.LOGGING_TYPE.getKey(), ContextMdcValue.LOGGING_TYPE_REQUEST.getValue()), "http request arrived."))
                .then(chain.filter(exchange));
    }
}
