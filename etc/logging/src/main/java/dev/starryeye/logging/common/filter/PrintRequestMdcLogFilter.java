package dev.starryeye.logging.common.filter;

import dev.starryeye.logging.common.ContextMdcKey;
import dev.starryeye.logging.common.LoggingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
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
            \n
            %s
            """;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (isPathNotMatch(exchange.getRequest().getPath())) {
            return chain.filter(exchange);
        }

        return extractRequestBody(exchange.getRequest())
                .flatMap(bodyBytes -> {
                    String body = bodyBytes.length > 0
                            ? new String(bodyBytes, StandardCharsets.UTF_8) : "<no body>";
                    printRequest(exchange.getRequest(), body);

                    ServerHttpRequest mutatedRequest = mutateExchangeWithBody(exchange, exchange.getRequest(), bodyBytes);
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    public static boolean isPathNotMatch(RequestPath path) {
        return PATH_PREFIXES.stream()
                .noneMatch(prefix -> path.pathWithinApplication().value().startsWith(prefix));
    }

    private Mono<byte[]> extractRequestBody(ServerHttpRequest request) {
        return DataBufferUtils.join(request.getBody())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .switchIfEmpty(Mono.just(new byte[0]));
    }

    private ServerHttpRequest mutateExchangeWithBody(ServerWebExchange exchange, ServerHttpRequest request, byte[] bodyBytes) {
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        return new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.just(bufferFactory.wrap(bodyBytes));
            }
        };
    }

    private void printRequest(ServerHttpRequest request, String body) {
        String method = request.getMethod().name();
        String url = request.getURI().toString();
        String headers = formatHeaders(request.getHeaders());

        log.info(
                appendEntries(getLoggingEntries()),
                HTTP_REQUEST_LOG_FORMAT.formatted(method, url, headers, body)
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
