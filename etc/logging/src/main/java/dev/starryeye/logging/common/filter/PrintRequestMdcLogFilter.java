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
//@Component
public class PrintRequestMdcLogFilter implements WebFilter { // 비활성화

    private static final List<String> PATH_PREFIXES = List.of("/articles");

    private static final String HTTP_REQUEST_LOG_FORMAT = "[FIRST REQUEST] method: %s, url: %s, headers: %s, body: %s";

    /**
     * DataBuffer::read 를 한번 수행하면 다음에는 읽을 수 없다.
     *
     * 동작 과정
     * 1. extractRequestBody 를 하면, request.getBody() 를 통해 DataBuffer::read 를 수행한다.
     * 2. getDecoratedRequest, exchange.mutate().request() 를 통해
     *      읽은 body 값을 그대로 넣은 DataBuffer 를 새로 만들고 해당 DataBuffer 를 이용할 수 있게 만든 ServerHttpRequestDecorator(getBody override..)를 다음 필터에 전달한다.
     * 3. 추후, argument resolver 에서 요청 데이터를 바인딩 할때 PrintRequestMdcLogFilter 에서 바꿔치기한 ServerHttpRequestDecorator 가 쓰이고 getBody 를 통해 정상적으로 읽게 된다.
     *
     *
     * 주의 사항.
     * application/json 을 명시한 json body 가 있는 POST 요청 시, 위 동작과정에 따라 잘 동작이 된다.
     * 하지만..
     * application/x-www-form-urlencoded 의 경우.. exchange.getFormData() 가 사용되고.. (내부에 request.getBody() 존재)
     * 아래와 같은 상황이 벌어진다.
     * 1. 요청이 오면 DefaultServerWebExchange 생성
     * 2. DefaultServerWebExchange 생성자 호출되며, DefaultServerWebExchange::initFormData 수행됨.
     *      call stack 을 쭉 따라가면, FormHttpMessageReader::readMono 가 수행됨.
     *      message.getBody() 가 지역변수 캡쳐됨.. (여기서의 message.getBody() 는 request.getBody() 로 생각하면 될듯..)
     * 3. PrintRequestMdcLogFilter::extractRequestBody 에서 request.getBody() 로 얻은 DataBuffer 를 읽고 해제시킴
     *      결국 exchange.getFormData() 를 수행하면 읽히기로 예정된 DataBuffer 가 없어진것이 된다.
     * 4. 추후, argument resolver 에서 요청 데이터를 바인딩 할때 exchange.getFormData 를 수행하면 지역변수 캡쳐된 Flux<DataBuffer> 로 DataBuffer 를 참조하면 이미 읽히고 없기 때문에.. body 가 없는 것 처럼 진행됨.
     *
     * 해결.
     * application/x-www-form-urlencoded 의 경우.. PrintRequestMdcLogFilter::extractRequestBody 에서 DataBuffer 를 읽고 해제 하기 전에
     * exchange.getFormData 로 먼저 읽어 드린다. FormHttpMessageReader::readMono 를 보면 cache() 를 하고 있기 때문에..
     * 추후, argument resolver 에서 getFormData 로 접근하면 DataBuffer 는 이미 읽혀서 더이상 읽을 수 가 없지만, cache 된 값이 리턴되어 정상 동작이된다.
     * -> PrintRequestMdcLogFilter2.java 를 보러가자
     */

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

                    ServerHttpRequestDecorator decorated = getDecoratedRequest(exchange, exchange.getRequest(), bodyBytes);
                    return chain.filter(exchange.mutate().request(decorated).build());
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
