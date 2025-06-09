package dev.starryeye.logging.common.filter;

import dev.starryeye.logging.common.ContextMdcKey;
import dev.starryeye.logging.common.LoggingType;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
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
import java.util.Map;
import java.util.stream.Collectors;

import static net.logstash.logback.marker.Markers.appendEntries;

@Slf4j
@Order(3)
@Component
public class PrintResponseMdcLogFilter implements WebFilter {

    private static final String RESPONSE_LOG_FORMAT = """
        \n[HTTP Response]
        %s
        %s
        Elapsed-Time: %d ms
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

        return new ServerHttpResponseDecorator(original) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                return super.writeWith(Flux.from(body)
                        .doOnComplete(() -> {
                            stopWatch.stop();
                            printResponse(getStatusCodeValue(), stopWatch.getTotalTimeMillis(), getHeaders());
                        })
                        .doOnError(ex -> {
                            stopWatch.stop();
                            printError(getStatusCodeValue(), stopWatch.getTotalTimeMillis(), getHeaders(), ex);
                        })
                );
            }

            private int getStatusCodeValue() {
                return getStatusCode() != null ? getStatusCode().value() : 200;
            }
        };
    }

    private void printResponse(int statusCode, long elapsed, HttpHeaders headers) {
        log.info(
                appendEntries(getLoggingEntries(statusCode, elapsed)),
                RESPONSE_LOG_FORMAT.formatted(statusCode, formatHeaders(headers), elapsed)
        );
    }

    private void printError(int statusCode, long elapsed, HttpHeaders headers, Throwable ex) {
        log.error(
                appendEntries(getLoggingEntries(statusCode, elapsed)),
                RESPONSE_LOG_FORMAT.formatted(statusCode, formatHeaders(headers), elapsed),
                ex
        );
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
