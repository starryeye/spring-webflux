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
     * chat project (without mongodb) 에서 v2 베이스로 고도화 하였다.
     *
     * todo, a 가 로그인하지 않은 b 에게 메시지를 보낸 이후,
     *  b 가 로그인을 하면 a 가 보낸 메시지는 전달 되지 않는다. 이를 해결 해보자.
     */

    private final ChatService chatService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        String from = (String) session.getAttributes().get("iam");

        Flux<Chat> chatFlux = chatService.register(from);
        chatService.sendChat(new Chat("System", from, from + "님 채팅방에 오신 것을 환영합니다."));
        log.info("{}가 채팅방에 입장하였습니다.", from);

        session.receive()
                .doOnNext(
                        webSocketMessage -> {

                            String payload = webSocketMessage.getPayloadAsText();

                            String[] splitPayload = payload.split(":");
                            String to = splitPayload[0].trim();
                            String message = splitPayload[1].trim();

                            chatService.sendChat(new Chat(from, to, message));
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
