package dev.starryeye.logging.common.filter;

import dev.starryeye.logging.common.ContextMdcKey;
import dev.starryeye.logging.common.LoggingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.logstash.logback.marker.Markers.append;
import static net.logstash.logback.marker.Markers.appendEntries;

@Slf4j
@Order(2)
@Component
public class PrintRequestMdcLogFilter implements WebFilter {

    private static final List<String> PATH_PREFIXES = List.of("/articles");

    private static final String HTTP_REQUEST_LOG_FORMAT = """
            \n[HTTP Request]
            %s %s
            %s
            """;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (isPathNotMatch(exchange.getRequest().getPath())) {
            return chain.filter(exchange);
        }

        printRequest(exchange);

        return chain.filter(exchange);
    }

    public static boolean isPathNotMatch(RequestPath path) {
        return PATH_PREFIXES.stream()
                .noneMatch(prefix -> path.pathWithinApplication().value().startsWith(prefix));
    }

    private void printRequest(ServerWebExchange exchange) {
        String method = exchange.getRequest().getMethod().name();
        String url = exchange.getRequest().getURI().toString();
        String headers = formatHeaders(exchange.getRequest().getHeaders());

        log.info(
                appendEntries(getLoggingEntries()),
                HTTP_REQUEST_LOG_FORMAT.formatted(method, url, headers)
        );
    }

    private Map<String, ? extends Serializable> getLoggingEntries() {
        return Map.of(
                ContextMdcKey.LOGGING_TYPE.getKey(), LoggingType.REQUEST.name()
        );
    }

    private String formatHeaders(HttpHeaders headers) {
        return headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.join(", ", entry.getValue()))
                .collect(Collectors.joining("\n"));
    }
}
