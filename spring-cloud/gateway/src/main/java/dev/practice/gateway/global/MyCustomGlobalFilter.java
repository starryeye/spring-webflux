package dev.practice.gateway.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
     * change the URI dynamically
     *
     * https://github.com/spring-cloud/spring-cloud-gateway/issues/3119
     *
     * 참고
     * https://yangbongsoo.tistory.com/14
     */

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
