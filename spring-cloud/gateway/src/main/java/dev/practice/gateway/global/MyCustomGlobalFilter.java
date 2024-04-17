package dev.practice.gateway.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Slf4j
@Component
public class MyCustomGlobalFilter implements GlobalFilter, Ordered {

    /**
     *
     * GlobalFilter
     * 조건에 따라 적용되지 않고 무조건 적용되는 필터이다.
     *
     * 특징
     * GatewayFilterFactory 로 생성하는 필터는 Order 가 1 로 셋팅된다.
     * GlobalFilter 는 Order 를 직접 설정할 수 있다.
     *
     * 공식 문서
     * https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway/global-filters.html
     */

    /**
     * Change the URI dynamically
     * - spring cloud gateway 에서는 application.yml 이나 설정으로 편하게..
     * URI 를 조건에따라 런타임에 switching 하는 기능이 없어서 아래와 같이 GlobalFilter 를 이용해야하는 것 같다..
     *
     * https://github.com/spring-cloud/spring-cloud-gateway/issues/3119
     * -> GATEWAY_REQUEST_URL_ATTR 를 이용하여 URI 를 변경시키고, Order 는 10001 로 해야 정확히 동작한다.
     * -> 즉, RouteToRequestUrlFilter(order : 10000)가 동작한 이후에 동작해야하는 것임
     *
     * 참고
     * https://yangbongsoo.tistory.com/14
     */

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // http body 에 "A" 가 포함되면 "http://localhost:8090", 그외 "http://localhost:8091" 로 요청하도록 한다.

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(
                        dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);

                            String bodyStr = new String(bytes);

                            ServerHttpRequest mutatedRequest;

                            if(bodyStr.contains("A")) {
                                mutatedRequest = exchange.getRequest()
                                        .mutate()
                                        .uri(URI.create("http://localhost:8090"))
                                        .build();
                            }else {
                                mutatedRequest = exchange.getRequest()
                                        .mutate()
                                        .uri(URI.create("http://localhost:8091"))
                                        .build();
                            }
                            exchange.getAttributes()
                                    .put(GATEWAY_REQUEST_URL_ATTR, mutatedRequest.getURI());
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        }
                ).switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 10001;
    }
}
