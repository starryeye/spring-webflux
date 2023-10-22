package dev.practice.sub2_sequence;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class Error {

    /**
     * [2]
     *
     * publisher 에서 error 를 subscriber 에게 전달한다.
     * onError 이벤트를 전달하고, 값으로는 Throwable 을 전달한다.
     *
     * 참고
     * publisher 와 subscribe 의 스레드는 생각하지 말자.
     * 주체가 어떤 것인지는 추후 Scheduler 에서 다루겠다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Mono.error(new RuntimeException("mono error"))
                .subscribe(value -> {
                    log.info("mono value: {}, tx: {}", value, Thread.currentThread().getName());
                }, error -> {
                    log.error("mono error: {}, tx: {}", error, Thread.currentThread().getName());
                });

        Flux.error(new RuntimeException("flux error"))
                .subscribe(value -> {
                    log.info("flux value: {}, tx: {}", value, Thread.currentThread().getName());
                }, error -> {
                    log.error("flux error: {}, tx: {}", error, Thread.currentThread().getName());
                });

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
