package dev.practice.sub4_error;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class DoOnError {

    /**
     * [8]
     *
     * doOnError
     *
     * 파이프 라인 흐름에 영향을 주지 않고 error logging 가능
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.error(new RuntimeException("error"))
                .doOnError( // error consumer
                        error -> log.info("doOnError error: {}, tx: {}", error, Thread.currentThread().getName())
                )
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("subscribe complete, tx: {}", Thread.currentThread().getName())
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
