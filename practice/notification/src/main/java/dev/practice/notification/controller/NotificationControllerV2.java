package dev.practice.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
@RestController
@RequestMapping("/api/notifications/v2")
public class NotificationControllerV2 {

    /**
     * Sinks.many() : Flux..
     * unicast() : 하나의 subscriber 가 존재한다.
     * onBackpressureBuffer() : ??..
     *
     * 흠..
     * 나중에 값을 집어넣을 수 있는
     * 파이프 라인만 일단 만들어두는 느낌..
     */
    private static final Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

    // 이벤트 생성 API
    @PostMapping
    public Mono<String> addNotification(@RequestBody Event event) {

        String notificationMessage = event.getType() + ": " + event.getMessage();

        log.info("addNotification v2, message: {}, tx: {}", notificationMessage, Thread.currentThread().getName());

        sink.tryEmitNext(notificationMessage); // 값을 집어 넣는다.

        return Mono.just("ok");
    }

    /**
     * Flux 의 타입 파라미터가 String 으로.. ServerSentEvent 가 아니기 때문에
     * String 이 data 로 그대로 내려간다.
     * 따라서, id 는 없으며.. "event" 타입은 "message"(기본 값) 로 내려간다.
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getNotifications() {

        // 클라이언트가.. 구독을 하고 있는 격이 된다...

        return sink.asFlux(); // sink 를 flux 로 넘겨준다.
    }
}
