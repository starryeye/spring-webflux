package dev.practice.webhandler.withwebfilter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
public class WebFilterPrev implements WebFilter {

    /**
     * WebFilter 는 함수형 인터페이스
     */

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        log.info("pre filter");

        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();

        // 요청 헤더 read
        String name = request.getHeaders()
                .getFirst("X-Custom-Name");

        if (name == null) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return response.setComplete(); // 필터링.. 바로 응답 처리
        } else {
            exchange.getAttributes().put("name", name); // attribute 에 "k: name, v: 헤더에서 읽은 값" 설정

            // 새로운 ServerHttpRequest 생성 (ServerHttpRequest 는 immutable 객체이다.)
            ServerHttpRequest newRequest = request.mutate()
                    .headers(httpHeaders -> httpHeaders.remove("X-Custom-Name"))
                    .build();

            // 새로운 ServerWebExchange 을 생성후 WebHandler 혹은 다음 필터로 넘긴다. (ServerWebExchange 도 immutable 객체이다.)
            return chain.filter(
                    exchange.mutate()
                            .request(newRequest)
                            .build()
            );
        }
    }
}
