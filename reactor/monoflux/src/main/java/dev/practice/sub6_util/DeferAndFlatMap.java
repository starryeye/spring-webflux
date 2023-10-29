package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class DeferAndFlatMap {

    /**
     * [10]
     *
     * 연산자 연습해보자.
     * Defer 와 FlatMap 연산자를 같이 사용한다면..
     *
     * flatMap(v -> Mono.defer(() -> Mono.just(v))) 는...
     * => flatMap(v -> Mono.just(v)) 와 동일하다.
     * => map(v -> v) 와 동일하다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Mono.just(1)
                .flatMap(
                        v -> Mono.defer(
                                () -> Mono.just(v)
                        )
                ).subscribe(
                        value -> log.info("subscribe next: {}, tx: {}", value, Thread.currentThread().getName())

                );
        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
