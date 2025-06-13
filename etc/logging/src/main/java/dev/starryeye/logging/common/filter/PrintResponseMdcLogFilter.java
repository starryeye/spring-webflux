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
import org.springframework.http.HttpStatusCode;
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

import static net.logstash.logback.marker.Markers.appendEntries;

@Slf4j
@Order(3)
@Component
public class PrintResponseMdcLogFilter implements WebFilter {

    private static final String HTTP_RESPONSE_LOG_FORMAT = "[FINAL RESPONSE] status: %d, headers: %s, elapsed: %d, body: %s";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (PrintRequestMdcLogFilter.isPathNotMatch(exchange.getRequest().getPath())) {
            return chain.filter(exchange);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ServerHttpResponseDecorator decorated = decorateResponse(exchange, stopWatch);
        return chain.filter(exchange.mutate().response(decorated).build())
                .doAfterTerminate(() -> {
                    /**
                     * 응답 데이터에 body 가 없으면 ServerHttpResponseDecorator::writeWith 가 호출되지 않아서 따로 처리해줘야한다.
                     * 마침 body 가 없으므로 dataBuffer 문제가 없으므로 그냥 출력하면 된다.
                     */
                    if (decorated.getDelegate().getHeaders().getContentLength() == 0) {
                        stopWatch.stop();
                        long elapsed = stopWatch.getTotalTimeMillis();
                        printResponse(decorated.getDelegate(), elapsed, "<no body>");
                    }
                });
    }

    private ServerHttpResponseDecorator decorateResponse(ServerWebExchange exchange, StopWatch stopWatch) {
        ServerHttpResponse original = exchange.getResponse();
        DataBufferFactory bufferFactory = original.bufferFactory();

        return new ServerHttpResponseDecorator(original) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) { // 참고, SSE 의 경우엔 writeAndFlushWith 를 오버라이딩할 것.
                return super.writeWith(Flux.from(body).flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    String bodyString = new String(bytes, StandardCharsets.UTF_8);

                    stopWatch.stop();
                    long elapsed = stopWatch.getTotalTimeMillis();

                    printResponse(getDelegate(), elapsed, bodyString);

                    return Mono.just(bufferFactory.wrap(bytes));
                }));
            }
        };
    }

    private void printResponse(ServerHttpResponse response, long elapsed, String bodyString) {

        HttpStatusCode statusCode = response.getStatusCode();
        int statusCodeValue = statusCode == null ? 0 : statusCode.value();
        HttpHeaders headers = response.getHeaders();

        log.info(
                appendEntries(getLoggingEntries(statusCodeValue, elapsed)),
                HTTP_RESPONSE_LOG_FORMAT.formatted(
                        statusCodeValue,
                        headers,
                        elapsed,
                        bodyString
                )
        );
    }

    private Map<String, ? extends Serializable> getLoggingEntries(int statusCode, Long elapsed) {
        return Map.of(
                ContextMdcKey.LOGGING_TYPE.getKey(), LoggingType.RESPONSE.name(),
                ContextMdcKey.STATUS_CODE.getKey(), statusCode,
                ContextMdcKey.TOTAL_ELAPSED_TIME.getKey(), elapsed
        );
    }
}
