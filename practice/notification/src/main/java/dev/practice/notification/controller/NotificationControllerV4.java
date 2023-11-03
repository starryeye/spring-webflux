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

    private final NotificationService notificationService;

    private static final AtomicInteger lastEventId = new AtomicInteger(1);


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

    @PostMapping
    public Mono<String> addNotification(@RequestBody Event event) {

        String notificationMessage = event.getType() + ": " + event.getMessage();

        notificationService.addMessageToSink(notificationMessage);

        return Mono.just("ok");
    }
}
