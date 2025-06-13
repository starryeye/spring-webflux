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

import static net.logstash.logback.marker.Markers.appendEntries;

@Slf4j
@Order(2)
@Component
public class PrintRequestMdcLogFilter2 implements WebFilter {

    private static final List<String> PATH_PREFIXES = List.of("/articles");

    private static final String HTTP_REQUEST_LOG_FORMAT = "[FIRST REQUEST] method: %s, url: %s, headers: %s, body: %s";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (isPathNotMatch(exchange.getRequest().getPath())) {
            return chain.filter(exchange);
        }

        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        if (isFormUrlEncoded(contentType)) {
            return handleFormUrlEncodedRequest(exchange, chain);
        }

        return handleJsonOrOtherRequest(exchange, chain);
    }

    public static boolean isPathNotMatch(RequestPath path) {
        return PATH_PREFIXES.stream()
                .noneMatch(prefix -> path.pathWithinApplication().value().startsWith(prefix));
    }

    private boolean isFormUrlEncoded(String contentType) {
        return contentType != null && contentType.contains("application/x-www-form-urlencoded");
    }

    private Mono<Void> handleFormUrlEncodedRequest(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getFormData().flatMap(formData -> {
            String body = formData.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                    .collect(Collectors.joining("&"));

            printRequest(exchange.getRequest(), body);
            return chain.filter(exchange); // formData는 재사용 가능
        });
    }

    private Mono<Void> handleJsonOrOtherRequest(ServerWebExchange exchange, WebFilterChain chain) {
        return extractRequestBody(exchange.getRequest())
                .flatMap(bodyBytes -> {
                    String body = bodyBytes.length > 0
                            ? new String(bodyBytes, StandardCharsets.UTF_8) : "<no body>";
                    printRequest(exchange.getRequest(), body);

                    ServerHttpRequestDecorator decorated =
                            getDecoratedRequest(exchange, exchange.getRequest(), bodyBytes);

                    return chain.filter(exchange.mutate().request(decorated).build());
                });
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

    private ServerHttpRequestDecorator getDecoratedRequest(ServerWebExchange exchange, ServerHttpRequest request, byte[] bodyBytes) {
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
        HttpHeaders headers = request.getHeaders();

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
}
