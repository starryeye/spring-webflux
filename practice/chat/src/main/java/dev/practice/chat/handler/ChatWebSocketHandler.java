package dev.practice.chat.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    // 참고. Map.of() 는 immutable 이다.
    // ConcurrentHashMap 은 mutable 이며, thread-safe
    private static final Map<String, Sinks.Many<Chat>> chatSinkMap = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        String from = (String) session.getAttributes().get("iam");
        Sinks.Many<Chat> sink = Sinks.many().unicast().onBackpressureBuffer();

        // todo, 생각해보기. 이 영역.. 채팅방 입장 로그 출력은.. 최초 연결 시점 1회만 실행된다.
        // 연결할 때, 요청자 별로 sink 를 가지게끔 한다.
        chatSinkMap.put(from, sink);
        sink.tryEmitNext(new Chat("System", from + "님 채팅방에 오신 것을 환영합니다."));
        log.info("{}가 채팅방에 입장하였습니다.", from);

        log.info("handle thread check: {}", Thread.currentThread().getName());


        // 요청 데이터를 타겟 사용자 sink 로 넘긴다.
        session.receive()
                .doOnNext(
                        webSocketMessage -> {
                            log.info("receive thread check: {}", Thread.currentThread().getName());

                            String payload = webSocketMessage.getPayloadAsText(); // 요청 데이터를 String 으로 변환

                            String[] splitPayload = payload.split(":");
                            String to = splitPayload[0].trim();
                            String message = splitPayload[1].trim();

                            Sinks.Many<Chat> targetSink = chatSinkMap.get(to);
                            if(Objects.nonNull(targetSink)) {

                                if(targetSink.currentSubscriberCount() > 0) { // 연결했다가 끊으면 chatSinkMap 에는 존재하지만.. 실제로 구독은 없어진다.
                                    targetSink.tryEmitNext(new Chat(from, message));
                                }else {
                                    chatSinkMap.remove(to); // 구독이 없어진 sink 는 삭제 해준다.
                                    sink.tryEmitNext(new Chat("System", "대화 상대가 없습니다."));
                                }
                            }else {
                                sink.tryEmitNext(new Chat("System", "대화 상대가 없습니다."));
                            }
                        }
                )
                .subscribe();
        /**
         * session.receive() 에 subscribe() 가 있어야하는 이유..
         * session.send() 와 엮어서 어렵게 생각하지말고..
         * 단순하게..
         * receive 즉, 요청 데이터의 소스(publisher) 의 이벤트를 받는 파이프라인을 실행시켰다고 생각하면 편하다. (앞으로 요청데이터가 들어오면, 해당 데이터로 doOnNext() 가 수행될 것이다.)
         * 실행 주체는 최초에 이 publisher, subscribe() 를 수행했던 handle 스레드이며..
         *
         * 참고로..
         * 이 스레드는 session.send() 도 수행한다.
         */

        // 요청자의 응답 데이터는 요청자의 sink 로 함.
        return session.send(
                sink.asFlux()
                        .map(
                                chat -> {
                                    log.info("send thread check: {}", Thread.currentThread().getName());

                                    return session.textMessage(chat.getFrom() + ": " + chat.getMessage());
                                }
                        )
        );
    }
}
