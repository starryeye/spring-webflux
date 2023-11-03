package dev.practice.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/api/notifications/v3")
public class NotificationControllerV3 {

    private static final Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

    private static final AtomicInteger lastEventId = new AtomicInteger(1);

    /**
     * 반환 타입이 .. Flux<ServerSentEvent> 로 ..
     * Flux 의 타입 파라미터가 ServerSentEvent<String> 이다.
     *
     * ServerSentEvent 로 chunk 를 좀더 구체적으로 전달 할 수 있게 되었다.
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getNotifications() {

        return sink.asFlux()
                .map(
                        message -> ServerSentEvent.builder(message)
                                .event("notification")
                                .id(String.valueOf(lastEventId.getAndIncrement()))
                                .comment("this is comment")
                                .build()
                );
    }

    @PostMapping
    public Mono<String> addNotification(@RequestBody Event event) {

        String notificationMessage = event.getType() + ": " + event.getMessage();

        log.info("addNotification v3, message: {}, tx: {}", notificationMessage, Thread.currentThread().getName());

        sink.tryEmitNext(notificationMessage);

        return Mono.just("ok");
    }
}
