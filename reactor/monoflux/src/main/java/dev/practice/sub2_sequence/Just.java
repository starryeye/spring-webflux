package dev.practice.sub2_sequence;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class Just {

    /**
     * [1]
     *
     * Publisher 가 파이프라인으로 주어진 객체를 전달한다.
     *
     * sequence 는 publisher 에서 값을 지속적으로 흘리는 모습을 뜻한다.
     *
     * 참고
     * publisher 와 subscribe 의 스레드는 생각하지 말자.
     * 주체가 어떤 것인지는 추후 Scheduler 에서 다루겠다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Mono.just(1)
                .subscribe(value -> {
                    log.info("mono value: {}, tx: {}", value, Thread.currentThread().getName());
                });

        Flux.just(1, 2, 3, 4, 5)
                .subscribe(value -> {
                    log.info("flux value: {}, tx: {}", value, Thread.currentThread().getName());
                });

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
