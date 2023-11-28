package dev.practice.chat.service;

import com.mongodb.client.model.changestream.OperationType;
import dev.practice.chat.entity.ChatDocument;
import dev.practice.chat.handler.Chat;
import dev.practice.chat.repository.ChatMongoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMongoRepository chatMongoRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private static final Map<String, Sinks.Many<Chat>> chatSinkMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void setup() {

        /**
         * ReactiveMongoTemplate 을 활용하여..
         *
         * 채팅 기록에 변경(insert) 이 생기면 doSend 를 수행한다.
         */

        log.info("hello chat service!");

        reactiveMongoTemplate.changeStream(ChatDocument.class)
                .listen()
                .doOnNext(
                        item -> {
                            ChatDocument target = Optional.ofNullable(item.getBody()).orElseThrow();
                            OperationType operationType = Optional.ofNullable(item.getOperationType()).orElseThrow();

                            log.info("target={}", target);
                            log.info("operationType={}", operationType);

                            if(operationType.equals(OperationType.INSERT)) {
                                doSend(new Chat(target.getFrom(), target.getTo(), target.getMessage()));
                            }
                        }
                )
                .subscribe();
    }

    public Flux<Chat> register(String from) {

        Sinks.Many<Chat> sink = Sinks.many().unicast().onBackpressureBuffer();

        chatSinkMap.put(from, sink);

        return sink.asFlux();
    }

    public void sendChat(Chat chat) {

        /**
         * MongoDb 에 채팅(채팅 기록)을 insert 한다.
         */

        log.info("sendChat, from={}, to={}, message={}", chat.getFrom(), chat.getTo(), chat.getMessage());

        chatMongoRepository.save(chat.toEntity())
                .doOnNext(saved -> log.info("saved={}", saved)) // 정상적으로 저장되었는지 확인
                .subscribe();
    }

    private void doSend(Chat chat) {

        /**
         * from 이 to 에게 실제로 message 를 보낸다.(sink flux 에 데이터를 흘린다.)
         *
         * 각 사용자는 자신의 pipeline (sink flux) 을 가지고 있다.
         */

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
