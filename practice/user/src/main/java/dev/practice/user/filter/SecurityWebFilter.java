package dev.practice.user.filter;

import dev.practice.user.auth.IamAuthentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

/**
 * bean 으로 등록만해도 WebFilter 로서, 동작하게 된다.
 *
 * 참고
 * Spring 의 도움없이 WebFilter 를 등록하는 과정은..
 * spring-webflux/webhandler/withwebfilter 코드를 보면 된다.
 */
@Component
public class SecurityWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        final ServerHttpResponse response = exchange.getResponse();

        String iam = exchange.getRequest()
                .getHeaders()
                .getFirst("X-I-AM");//multiValue 이므로 first 로 접근

        if(iam == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // 401
            return response.setComplete(); // 바로 응답을 내림
        }

        Authentication authentication = new IamAuthentication(iam); // 헤더 값을 name 필드로 사용

        return chain.filter(exchange) // WebHandler 혹은 다음 WebFilter 로 넘긴다.
                .contextWrite( // context 로 authentication 등록
                        context -> {

                            // 기존의 context 를 건드리지 않고 authentication 을 포함시켜 새로운 context 를 만들어 전달
                            ContextView newContext = ReactiveSecurityContextHolder
                                    .withAuthentication(authentication);
                            return context.putAll(newContext);
                        }
                );
    }
}
