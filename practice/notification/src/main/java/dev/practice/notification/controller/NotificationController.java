package dev.practice.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/notifications/v1")
public class NotificationController {

    /**
     * Server Sent Event
     * - Http streaming 기법이다.
     * - Chunked Transfer-Encoding 기반이다.
     * - chunk 단위(여러 줄로 구성된 문자열이며 new line 으로 서로를 구분)로 event 를 구분
     * 즉, chunk 하나가 event 하나
     * - 문자열은 일반적으로 field: value 로 구성
     */


    /**
     * Server Sent Event 를 구현하기 위한 조건
     *
     * 1. Content-Type: text/event-stream
     * -> server sent event 의 조건이다.
     *
     * 2. Flux<String> 을 리턴타입으로 사용 (Flux 의 타입 파라미터가 ServerSentEvent 가 아님)
     * -> Chunk 단위 내에서 id 는 없고.. "event" 타입은 "message"(기본 값) 로 전달하도록 함
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
