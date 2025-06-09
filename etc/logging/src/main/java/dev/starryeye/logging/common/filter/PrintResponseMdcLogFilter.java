package dev.starryeye.logging.common.filter;

import dev.starryeye.logging.common.ContextMdcKey;
import dev.starryeye.logging.common.LoggingType;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static net.logstash.logback.marker.Markers.appendEntries;

@Slf4j
@Order(3)
@Component
public class PrintResponseMdcLogFilter implements WebFilter {

    private static final String HTTP_RESPONSE_LOG_FORMAT = """
            \n[HTTP Response]
            %s
            %s
            Elapsed-Time: %d ms
            \n
            %s
            """;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ServerHttpResponseDecorator decorated = decorateResponse(exchange, stopWatch);
        return chain.filter(exchange.mutate().response(decorated).build());
    }

    private ServerHttpResponseDecorator decorateResponse(ServerWebExchange exchange, StopWatch stopWatch) {
        ServerHttpResponse original = exchange.getResponse();
        DataBufferFactory bufferFactory = original.bufferFactory();

        return new ServerHttpResponseDecorator(original) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                return super.writeWith(Flux.from(body).flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    String bodyString = new String(bytes, StandardCharsets.UTF_8);

                    stopWatch.stop();
                    int statusCode = getStatusCodeValue();
                    long elapsed = stopWatch.getTotalTimeMillis();

                    log.info(
                            appendEntries(getLoggingEntries(statusCode, elapsed)),
                            HTTP_RESPONSE_LOG_FORMAT.formatted(
                                    statusCode,
                                    formatHeaders(exchange.getRequest().getHeaders()),
                                    elapsed,
                                    bodyString
                            )
                    );

                    return Mono.just(bufferFactory.wrap(bytes));
                }));
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return super.writeAndFlushWith(Flux.from(body).map(inner -> Flux.from(inner).flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return Mono.just(bufferFactory.wrap(bytes));
                })));
            }

            private int getStatusCodeValue() {
                return getStatusCode() != null ? getStatusCode().value() : 200;
            }
        };
    }

    private Map<String, ? extends Serializable> getLoggingEntries(int statusCode, Long elapsed) {
        return Map.of(
                ContextMdcKey.LOGGING_TYPE.getKey(), LoggingType.RESPONSE.name(),
                ContextMdcKey.STATUS_CODE.getKey(), statusCode,
                ContextMdcKey.TOTAL_ELAPSED_TIME.getKey(), elapsed
        );
    }

    private String formatHeaders(HttpHeaders headers) {
        return headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.join(", ", entry.getValue()))
                .collect(Collectors.joining("\n"));
    }
}
