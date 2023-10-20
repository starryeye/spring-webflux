package dev.practice.webhandler.withwebfilter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
public class WebFilterPost implements WebFilter {

    /**
     * WebFilter 는 함수형 인터페이스
     *
     * Mono::doOnSuccess 를 활용하여..
     * Servlet stack HandlerInterceptor 의 postHandle 과 동일하게 사용 가능하다.
     */

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {


        return chain.filter(exchange)
                .doOnSuccess(v -> {
                    log.info("after filter");
                });
    }
}
