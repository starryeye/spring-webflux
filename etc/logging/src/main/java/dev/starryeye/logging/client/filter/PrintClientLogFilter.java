package dev.starryeye.logging.client.filter;

import dev.starryeye.logging.common.ContextMdcKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

import static net.logstash.logback.marker.Markers.appendEntries;

@Slf4j
public abstract class PrintClientLogFilter {

    private PrintClientLogFilter() {
    }

    private static final String HTTP_REQUEST_LOG_FORMAT = """
            \n[External API HTTP Request]
            %s %s
            %s
            """;

    private static final String HTTP_RESPONSE_LOG_FORMAT = """
            \n[External API HTTP Response]
            %d
            %s
            Elapsed-Time: %d ms
            """;

    public static ExchangeFilterFunction getFilter() {
        return (request, next) -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            log.info(HTTP_REQUEST_LOG_FORMAT.formatted(
                    request.method(),
                    request.url().getPath() + (request.url().getQuery() != null ? "?" + request.url().getQuery() : ""),
                    formatHeaders(request.headers())
            ));

            return next.exchange(request)
                    .doOnNext(response -> {
                        stopWatch.stop();
                        long elapsed = stopWatch.getTotalTimeMillis();

                        log.info(appendEntries(getResponseLoggingEntries(request, response, elapsed)),
                                HTTP_RESPONSE_LOG_FORMAT.formatted(
                                        response.statusCode().value(),
                                        formatHeaders(response.headers().asHttpHeaders()),
                                        elapsed
                                ));
                    })
                    .doOnError(throwable -> {
                        stopWatch.stop();
                        long elapsed = stopWatch.getTotalTimeMillis();

                        log.error(appendEntries(getErrorLoggingEntries(request, elapsed)),
                                "[HTTP Error] request to {} failed in {} ms",
                                request.url(),
                                elapsed,
                                throwable
                        );
                    });
        };
    }

    private static Map<? extends Serializable, ? extends Comparable<? extends Comparable<?>>> getResponseLoggingEntries(ClientRequest request, ClientResponse response, long elapsed) {
        return Map.of(
                ContextMdcKey.EXTERNAL_API_URL.getKey(), request.url(),
                ContextMdcKey.EXTERNAL_API_STATUS_CODE.getKey(), response.statusCode().value(),
                ContextMdcKey.EXTERNAL_API_ELAPSED_TIME.getKey(), elapsed
        );
    }

    private static Map<? extends Serializable, ? extends Comparable<? extends Comparable<?>>> getErrorLoggingEntries(ClientRequest request, long elapsed) {
        return Map.of(
                ContextMdcKey.EXTERNAL_API_URL.getKey(), request.url(),
                ContextMdcKey.EXTERNAL_API_ELAPSED_TIME.getKey(), elapsed
        );
    }

    private static String formatHeaders(HttpHeaders headers) {
        return headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.join(", ", entry.getValue()))
                .collect(Collectors.joining("\n"));
    }
}