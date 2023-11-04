package dev.practice.websocket.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class EchoWebSocketHandler implements WebSocketHandler {
    @Override
    public Mono<Void> handle(WebSocketSession session) {

        Flux<WebSocketMessage> echoFlux = session.receive()
                .map(
                        webSocketMessage -> {

                            String message = webSocketMessage.getPayloadAsText();
                            log.info("receive message: {}, tx: {}", message, Thread.currentThread().getName());

                            return session.textMessage("echo " + message);
                        }
                );

        return session.send(echoFlux);
    }
}
