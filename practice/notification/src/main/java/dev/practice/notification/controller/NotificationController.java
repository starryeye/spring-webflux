package dev.practice.notification.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    /**
     * Server Sent Event 를 구현하기 위한 조건
     *
     * 1. Content-Type: text/event-stream
     * -> server sent event 의 조건이다.
     *
     * 2. Flux<String> 을 리턴타입
     * -> Chunk 단위 내에서 id 는 없고.. event 타입은 message(기본 값) 로 전달하도록 함
     * ServerSentEvent 객체의 data 에 String 을 집어 넣고 쭉 흘려보낸다.
     *
     * 3. @ResponseBody
     * -> ResponseBodyResultHandler 가 응답을 처리하도록 함
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getNotifications() {

        // 1초에 한번씩 Hello world 를 흘려보낸다.
        return Flux.interval(Duration.ofMillis(1000))
                .map(value -> "Hello world");
    }
}
