package dev.practice.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.function.Predicate;

@Component
public class MyCustomRoutePredicateFactory extends AbstractRoutePredicateFactory<MyCustomRoutePredicateFactory.Config> {

    /**
     * Spring 이 기본적으로 제공하는 Route predicate 외에 커스텀으로 만들고 싶을 때 이와 같이 하면 된다.
     * - AbstractRoutePredicateFactory 상속 (RoutePredicateFactory 는 함수형인터페이스)
     * - AbstractRoutePredicateFactory 를 상속하는 클래스 이름은 predicate name 을 앞쪽에 명시해야한다. ("MyCustom"RoutePredicateFactory)
     *      XXXRoutePredicateFactory 를 바탕으로 predicate name 을 매칭한다. (MyCustom 이 predicate name)
     * - 비슷하게 predicate args 는 Config 의 프로퍼티 이름으로 접근한다. (greeting 이 predicate args)
     *
     * 위 내용 까지는 fully expanded 방식으로 application.yml 을 작성할 경우이다.
     *
     * 아래는 shortcut 방식으로 작성할 경우에 필요하다. (두가지 방식을 모두 지원해주도록 하자)
     * - shortcutFieldOrder 메서드 오버라이딩 필요
     */

    public MyCustomRoutePredicateFactory() { // 필수
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) { // 필수, predicate 실제 조건 구현

        return serverWebExchange -> {

            String path = serverWebExchange.getRequest()
                    .getPath().toString();

            String greeting = config.getGreeting();

            return path.contains(greeting); // request path 에 greeting 에 담긴 문자열이 포함되어있는가..
        };
    }

    @Override
    public List<String> shortcutFieldOrder() { // shortcut 방식으로 작성 지원용
        return List.of("greeting"); // predicate 에서 MyCustom=~~~~ 으로 작성할 때.. ~~~~ 에 들어갈 값의 key 에 해당 (fully expanded predicate args 에 해당)
    }

    @Setter // for binding
    @Getter
    public static class Config { // apply 메서드 파라미터로 필요한 Config class
        private String greeting; // predicate args
    }
}
