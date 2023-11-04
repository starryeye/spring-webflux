package dev.practice.chat.v2.service;

import dev.practice.chat.v2.handler.Chat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatService {

    private static final Map<String, Sinks.Many<Chat>> chatSinkMap = new ConcurrentHashMap<>();

    public Flux<Chat> register(String from) {

        Sinks.Many<Chat> sink = Sinks.many().unicast().onBackpressureBuffer();

        chatSinkMap.put(from, sink);

        sink.tryEmitNext(new Chat("System", from + "님 채팅방에 오신 것을 환영합니다."));
        log.info("{}가 채팅방에 입장하였습니다.", from);

        return sink.asFlux();
    }

    public boolean sendChat(String to, Chat chat) {

        Sinks.Many<Chat> targetSink = chatSinkMap.get(to);

        if(Objects.nonNull(targetSink)) {

            if(targetSink.currentSubscriberCount() > 0) {
                targetSink.tryEmitNext(chat);
                return true;
            }else {
                chatSinkMap.remove(to);
            }
        }

        return false;
    }
}
