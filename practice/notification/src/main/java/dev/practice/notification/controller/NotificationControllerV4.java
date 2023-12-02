package dev.practice.notification.controller;

import dev.practice.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/notifications/v4")
public class NotificationControllerV4 {

    /**
     * Sinks, ServerSentEvent 를 활용하여 HTTP Streaming 기법을 구현한다.
     */

    private final NotificationService notificationService;

    private static final AtomicInteger lastEventId = new AtomicInteger(1);


    // client 에서 특정 pipline(Sinks) 이벤트를 구독한다.
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getNotifications() {

        return notificationService.getMessageFromSink()
                .map(
                        message -> ServerSentEvent.builder(message)
                                .id(String.valueOf(lastEventId.getAndIncrement()))
                                .event("notification")
                                .comment("this is comment")
                                .build()
                );
    }

    // 외부에서 이벤트가 발생되어 해당 서버로 알리는 역할의 API 이다.
    // 이벤트가 발생했으므로 해당 이벤트를 구독하는 client 로 데이터를 흘려보내준다.
    @PostMapping
    public Mono<String> addNotification(@RequestBody Event event) {

        String notificationMessage = event.getType() + ": " + event.getMessage();

        notificationService.addMessageToSink(notificationMessage);

        return Mono.just("ok");
    }
}
