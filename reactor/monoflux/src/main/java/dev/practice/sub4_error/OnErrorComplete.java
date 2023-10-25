package dev.practice.sub4_error;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class OnErrorComplete {

    /**
     * [6]
     *
     * onErrorComplete 연산자
     * - onError 이벤트를 onComplete 이벤트로 변경하여 전달한다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.create(
                        sink -> {
                            sink.next(1);
                            sink.next(2);
                            sink.error(new RuntimeException("error"));
                            sink.next(3);
                        }
                )
                .onErrorComplete() // onError 이벤트를 onComplete 이벤트로 변경한다.
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("subscribe complete, tx: {}", Thread.currentThread().getName())
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
