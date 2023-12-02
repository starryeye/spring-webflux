package dev.practice.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
public class NotificationService {

    private final ReactiveStreamOperations<String, String, String> reactiveStreamOperations;

    private static final String STREAM_NAME = "notification:1";
    private static final String HASH_KEY = "message";

    private static final Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

    public NotificationService(
            ReactiveStringRedisTemplate reactiveStringRedisTemplate,
            ReactiveRedisConnectionFactory reactiveRedisConnectionFactory
    ) {

        // ReactiveStreamOperations init
        this.reactiveStreamOperations = reactiveStringRedisTemplate.opsForStream();

        // StreamReceiver init
        StreamReceiver.StreamReceiverOptions<String, MapRecord<String, String, String>> options = StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(Duration.ofMillis(100L)) // redis 에 접근하여, 얼마나 자주 데이터를 가져올것인가. (default 2s) 즉, busy-wait, pull model
                .build();
        StreamReceiver<String, MapRecord<String, String, String>> streamReceiver = StreamReceiver.create(reactiveRedisConnectionFactory, options);

        // StreamReceiver 작업 개발
        // Redis Stream 을 활용하여 redis 에 이벤트를 저장하고 receive 하여 Sinks 에 이벤트를 흘려보내줌 (receive 하여 Sinks 에 이벤트 흘려보내는 부분)
        streamReceiver.receive(StreamOffset.create(STREAM_NAME, ReadOffset.latest()))
                .subscribe( // todo, main 에서 subscribe 를 했는데 lettuce-nioEventLoop 에 의해서 실행된다. 내부적으로 subscribeOn 이 걸려있는 것 같다...
                        mapRecord -> {
                            log.info("record={}, tx={}", mapRecord, Thread.currentThread().getName());
                            String notificationMessage = mapRecord.getValue().get(HASH_KEY);

                            sink.tryEmitNext(notificationMessage);
                        }
                );
    }

    public Flux<String> getMessageFromSink() {

        return sink.asFlux();
    }

    public void addMessageToSink(String notificationMessage) {

        log.info("addNotification, message: {}, tx: {}", notificationMessage, Thread.currentThread().getName());

        // Redis Stream 을 활용하여 redis 에 이벤트를 저장하고 receive 하여 Sinks 에 이벤트를 흘려보내줌 (redis 에 이벤트 저장하는 부분 자료 구조는 Redis Stream 사용)
        reactiveStreamOperations.add(STREAM_NAME, Map.of(HASH_KEY, notificationMessage))
                        .subscribe();
    }
}
