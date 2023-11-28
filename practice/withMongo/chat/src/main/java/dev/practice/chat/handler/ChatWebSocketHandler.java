package dev.practice.chat.handler;

import dev.practice.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    /**
     * chat project (without mongodb) 에서 v2 베이스이다.
     */

    private final ChatService chatService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        String from = (String) session.getAttributes().get("iam");

        Flux<Chat> chatFlux = chatService.register(from);


        session.receive()
                .doOnNext(
                        webSocketMessage -> {

                            String payload = webSocketMessage.getPayloadAsText();

                            String[] splitPayload = payload.split(":");
                            String to = splitPayload[0].trim();
                            String message = splitPayload[1].trim();

                            boolean result = chatService.sendChat(to, new Chat(from, message));
                            if(!result) {
                                chatService.sendChat(from, new Chat("System", "대화 상대가 없습니다."));
                            }
                        }
                )
                .subscribe();

        return session.send(
                chatFlux
                        .map(
                                chat -> session.textMessage(chat.getFrom() + ": " + chat.getMessage())
                        )
        );
    }
}
