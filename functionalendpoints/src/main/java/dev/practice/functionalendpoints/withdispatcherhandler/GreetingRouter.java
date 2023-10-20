package dev.practice.functionalendpoints.withdispatcherhandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class GreetingRouter {

    /**
     * RouterFunction, HandlerFunction 은 Spring 5 에 추가된 Spring Webflux 의 일부 이다.
     * Reactive stack 에서 사용 가능한 RESTful 서비스를 구현하는 방법 중 하나이다.
     *
     * RouterFunction 과 HandlerFunction 이 합쳐져서 @Controller 개념이 된다.
     *
     * Router 는 서버로 들어오는 요청을 적절한 Handler 로 연결한다.
     * Handler 는 요청을 받고 응답을 반환한다.
     */

    @Bean
    public RouterFunction<ServerResponse> route(GreetingHandler greetingHandler) {
        // RouterFunction 을 빈으로 등록한다.
        // (GreetingHandler Bean 주입) @Bean 에서 사용 가능한 메서드 주입 방식 이다.

        return RouterFunctions.route( // RouterFunction 을 구현
                        RequestPredicates.GET("/hello")
                                .and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
                        greetingHandler::hello // 메서드 레퍼런스, HandlerFunction 의 구현
                );
    }
}
