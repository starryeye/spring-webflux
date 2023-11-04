package dev.practice.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class WebSocketConfig {

    // WebSocketHandlerAdapter 에 WebSocketService 를 등록한다.
    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter(webSocketService());
    }


    /**
     * WebSocketService..
     * - WebFilter 와 비슷한 역할을 한다.
     * - WebSocketHandler 를 사용할 때는 WebFilter 를 사용할 수 없다. (WebSocketHandler 는 WebHandler 가 아니므로..)
     */
    @Bean
    public WebSocketService webSocketService() {
        HandshakeWebSocketService webSocketService = new HandshakeWebSocketService() {
            @Override
            public Mono<Void> handleRequest(ServerWebExchange exchange, WebSocketHandler handler) {

                // custom filtering
                String iam = exchange.getRequest().getHeaders().getFirst("X-I-AM");
                if (Objects.isNull(iam)) { // X-I-AM 헤더가 없으면 연결이 끊긴다. 응답은 401
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                //todo, putAll() 이나.. immutable 객체 관련해서 문제 없는건가..
                return exchange.getSession()
                        .flatMap(
                                webSession -> {
                                    webSession.getAttributes().put("iam", iam);

                                    return super.handleRequest(exchange, handler);
                                }
                        );
            }
        };

        // 넣어준 attribute 가 WebSocketHandler 로 넘어갈 수 있도록 설정
        webSocketService.setSessionAttributePredicate(s -> true);

        return webSocketService;
    }
}
