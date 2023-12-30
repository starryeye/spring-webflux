package dev.practice.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Component // 빈 등록해줘야함
public class MyFilterGatewayFilterFactory extends AbstractGatewayFilterFactory<MyFilterGatewayFilterFactory.Config> {

    /**
     *
     * GatewayFilter 직접 만들어보기.
     * - filter 이름은 MyFilter 이다.
     *
     * predicate 를 직접 만든..
     * MyPredicateRoutePredicateFactory 와 개념이 유사하다.
     */


    public MyFilterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) { // filter 구현 부, PrefixPath 와 동작이 동일

        return ((exchange, chain) -> {

            String path = exchange.getRequest().getPath().value();

            ServerHttpRequest nextRequest = exchange.getRequest()
                    .mutate()
                    .path("/" + config.getGreeting() + path) // greeting 값을 prefix 로 기존 path 에 붙인다.
                    .build();

            ServerWebExchange next = exchange.mutate()
                    .request(nextRequest)
                    .build();

            return chain.filter(next);
        });
    }

    @Override
    public List<String> shortcutFieldOrder() { // shortcut 용
        return List.of("greeting");
    }

    @Getter
    @Setter
    public static class Config {
        private String greeting;
    }
}
