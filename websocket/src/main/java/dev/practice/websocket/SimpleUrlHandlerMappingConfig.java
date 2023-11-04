package dev.practice.websocket;

import dev.practice.websocket.webhandler.GreetWebHandler;
import dev.practice.websocket.websocket.GreetWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

@Configuration
public class SimpleUrlHandlerMappingConfig {

    /**
     * WebSocket..
     * - OSI 7 계층에 위치하는 프로토콜이다.
     *  - TCP (4 계층) 에 의존한다.
     * - 하나의 TCP 연결로 실시간 양방향 통신을 지원한다.
     * - 다른 OSI 7 계층 프로토콜인 HTTP 와 비교하여 지속적인 연결을 유지하면 오버헤드가 적다
     * - 최초 연결 시에는 HTTP 프로토콜이 사용된다. 이후, WebSocket 프로토콜 사용
     *  - 최초 연결때의 HTTP 요청/응답 에 아래의 헤더가 존재한다.
     *   - Upgrade: websocket
     *   - Connection: Upgrade
     * - WebSocket 을 이용하면..
     *  - DispatcherHandler 가.. 아래를 이용한다.
     *   - SimpleUrlHandlerMapping
     *   - WebSocketHandlerAdapter
     *
     *
     * Http streaming 과 비교..
     * - Http streaming 은 단방향이다.
     * - Http streaming 은 WebSocket 에 비해 지속적인 연결을 유지할 경우 오버헤드가 크다
     */


    /**
     * DispatcherHandler 가 사용할..
     * SimpleUrlHandlerMapping 을 직접 구현하여 Bean 으로 등록한다.
     * - SimpleUrlHandlerMapping 은 WebHandler, WebSocketHandler 를 지원한다.
     */
    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {

        Map<String, Object> urlMap = Map.of(
                "/simple-url-handler-mapping/web-handler/greet", new GreetWebHandler(), // key 로 매핑될 url, value 로 매핑시켜줄 WebHandler 를 등록하였다.
                "/simple-url-handler-mapping/web-socket-handler/greet", new GreetWebSocketHandler()
        );

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(1);
        mapping.setUrlMap(urlMap);

        return mapping;
    }

}
