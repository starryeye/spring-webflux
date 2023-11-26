package dev.practice.user.filter;

import dev.practice.user.auth.IamAuthentication;
import dev.practice.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

/**
 * bean 으로 등록만해도 WebFilter 로서, 동작하게 된다.
 * <p>
 * 참고
 * Spring 의 도움없이 WebFilter 를 등록하는 과정은..
 * spring-webflux/webhandler/withwebfilter 코드를 보면 된다.
 */

@RequiredArgsConstructor
@Component
public class SecurityWebFilter implements WebFilter {

    /**
     * - X-I-AM 헤더 없으면 UNAUTHORIZED
     * - X-I-AM 헤더 값(토큰)이 AuthService 의 tokenUserIdMap 에 존재하지 않는 key 라면 UNAUTHORIZED
     * - X-I-AM 헤더 값(토큰)이 AuthService 의 tokenUserIdMap 에 존재하는 key 라면
     * userId 를 Authentication (spring security lib) 에서 Principal (java lib) 의 name 으로 사용하여 계속 진행
     */

    private final AuthService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        final ServerHttpResponse response = exchange.getResponse();

        String iam = exchange.getRequest()
                .getHeaders()
                .getFirst("X-I-AM");//multiValue 이므로 first 로 접근

        //임시로.. 회원 가입은 pass 시킨다. -> todo spring security PreAuthorize 로 고도화 가능
        if(exchange.getRequest().getURI().getPath().equals("/api/users/signup")) {
            return chain.filter(exchange);
        }


        if (iam == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // 401
            return response.setComplete(); // 바로 응답을 내림
        }

        return authService.getNameByToken(iam)
                .map(IamAuthentication::new) // userId 값을 IamAuthentication name 필드로 사용
                .flatMap(
                        iamAuthentication -> chain.filter(exchange) // WebHandler 혹은 다음 WebFilter 로 넘긴다.
                                .contextWrite( // context 로 authentication 등록
                                        context -> {

                                            // 기존의 context 를 건드리지 않고 authentication 을 포함시켜 새로운 context 를 만들어 전달
                                            ContextView newContext = ReactiveSecurityContextHolder
                                                    .withAuthentication(iamAuthentication);
                                            return context.putAll(newContext);
                                        }
                                )
                )
                .switchIfEmpty( // getNameByToken 에서 Mono.empty 로 내려올 경우..
                        Mono.defer(
                                () -> {
                                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                                    return response.setComplete(); // 바로 응답을 내림
                                }
                        )
                );
    }
}
