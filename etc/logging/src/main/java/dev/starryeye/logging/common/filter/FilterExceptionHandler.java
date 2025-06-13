package dev.starryeye.logging.common.filter;

import dev.starryeye.logging.common.ContextMdcKey;
import dev.starryeye.logging.common.LoggingType;
import dev.starryeye.logging.common.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static net.logstash.logback.marker.Markers.appendEntries;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class FilterExceptionHandler implements WebFilter {

    private static final String HTTP_RESPONSE_LOG_FORMAT = "[FINAL RESPONSE, FILTER ERROR] status: %d, headers: %s, body: %s";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        /**
         * 필터에서 발생한 예외는 BusinessExceptionHandler 가 처리하지 못하여
         * 필터 예외를 처리하는 공통 예외 핸들러를 생성
         *
         * SetRequestMdcFilter 의 contextWrite 보다 upstream 에 있어야 ContextMdc 가 동작하므로..
         * SetRequestMdcFilter 보다 우선순위가 낮아야한다.
         */

        return chain.filter(exchange)
                .onErrorResume(ex -> {

                    log.error("Filter 에서 발생한 Exception 을 처리 한다.", ex);

                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                    String errorCode = ex.getClass().toString();
                    String description = ex.getMessage();
                    String responseBody = ExceptionResponse.toString(errorCode, description);

                    printResponse(exchange.getResponse(), responseBody);

                    DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
                    return exchange.getResponse().writeWith(Mono.just(dataBuffer));
                });
    }

    private void printResponse(ServerHttpResponse response, String body) {
        HttpStatusCode statusCode = response.getStatusCode();
        int statusCodeValue = statusCode == null ? 0 : statusCode.value();
        HttpHeaders headers = response.getHeaders();

        log.info(appendEntries(getLoggingEntries(statusCodeValue)),
                HTTP_RESPONSE_LOG_FORMAT.formatted(statusCodeValue, headers, body));
    }

    private Map<String, ? extends Serializable> getLoggingEntries(int statusCode) {
        return Map.of(
                ContextMdcKey.LOGGING_TYPE.getKey(), LoggingType.RESPONSE.name(),
                ContextMdcKey.STATUS_CODE.getKey(), statusCode
        );
    }
}
