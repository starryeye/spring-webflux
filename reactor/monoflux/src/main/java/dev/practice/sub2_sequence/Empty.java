package dev.practice.sub2_sequence;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class Empty {

    /**
     * [3]
     *
     * Mono.empty, Flux.empty 를 통해서 subscriber 에게 onComplete 이벤트를 전달한다.
     *
     * 값이 존재하지 않으므로..
     * 파이프라인 중간에 값을 필요로하는 작업은 수행하지 않는다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Mono.empty()
                .doOnNext(
                        // 값이 존재하지 않으므로 해당 파이프라인 작업(doOnNext) 는 수행되지 않는다.
                        value -> log.info("mono doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe(
                        value -> log.info("mono value: {}, tx: {}", value, Thread.currentThread().getName()),
                        null,
                        () -> log.info("mono complete, tx: {}", Thread.currentThread().getName())
                );

        Flux.empty()
                .doOnNext(
                        // 값이 존재하지 않으므로 해당 파이프라인 작업(doOnNext) 는 수행되지 않는다.
                        value -> log.info("flux doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe(
                        value -> log.info("flux value: {}, tx: {}", value, Thread.currentThread().getName()),
                        null,
                        () -> log.info("flux complete, tx: {}", Thread.currentThread().getName())
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
