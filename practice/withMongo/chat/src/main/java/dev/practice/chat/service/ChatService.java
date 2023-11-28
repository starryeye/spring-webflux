package dev.practice.chat.service;

import dev.practice.chat.handler.Chat;
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

        return sink.asFlux();
    }

    public void sendChat(Chat chat) {

        Sinks.Many<Chat> targetSink = chatSinkMap.get(chat.getTo());

        if(Objects.nonNull(targetSink)) {

            if(targetSink.currentSubscriberCount() > 0) {
                targetSink.tryEmitNext(chat);
            }else {
                chatSinkMap.remove(chat.getTo());
            }
        } else {

            Sinks.Many<Chat> mySink = chatSinkMap.get(chat.getFrom());
            mySink.tryEmitNext(new Chat("System", chat.getFrom(), "대화 상대가 없습니다."));
        }
    }
}
