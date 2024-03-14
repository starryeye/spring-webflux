package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Defer {

    /**
     * [9]
     *
     * defer 연산자에 대해 알아본다.
     * public static <T> Mono<T> defer(Supplier<? extends Mono<? extends T>> supplier)
     *
     * - publisher 를 생성하는 Supplier 를 인자로 받는다.
     * - 생성된 publisher 의 이벤트를 downstream 으로 전달한다.
     * - flatMap 과 차이점은.. 둘다 publisher 를 반환하는 것은 동일하지만..
     * flatMap 은 Functional 이지만, defer 는 Supplier 이다.
     * -> flatMap 은 파이프라인 중간에 위치하며 onNext 에 의해 반응
     * -> defer 는 파이프라인 중간에 위치하지는 않으며, publisher 를 생성해내는 역할이다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Mono.defer( // defer 로 Mono 를 반환하고 반환한 Mono 의 이벤트를 downstream 으로 전달한다.
                () -> Mono.just(1) // Mono.defer(() -> Mono.just(1)) 은 Mono.just(1) 과 동일하다.
        ).subscribe(
                value -> log.info("subscribe next: {}, tx: {}", value, Thread.currentThread().getName())
        );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
