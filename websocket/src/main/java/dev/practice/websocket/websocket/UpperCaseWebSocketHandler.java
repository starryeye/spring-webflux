package dev.practice.websocket.websocket;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UpperCaseWebSocketHandler implements WebSocketHandler {

    // WebSocketHandler 구현
    @Override
    public Mono<Void> handle(WebSocketSession session) {

        // WebSocketHandler 에서는 요청/응답 데이터의 기본 단위가 WebSocketMessage 이다.
        Flux<WebSocketMessage> receive = session.receive() // 요청 데이터를 stream 으로 받는다.
                .map(WebSocketMessage::getPayloadAsText) // WebSocketMessage(요청 데이터) 를 String 으로 변환
                .map(String::toUpperCase)
                .map(session::textMessage); // String 을 WebSocketMessage 로 변환

        return session.send(receive); // 결국 요청 데이터를 받으면.. 대문자로 변환하여 응답으로 준다.
    }
}
