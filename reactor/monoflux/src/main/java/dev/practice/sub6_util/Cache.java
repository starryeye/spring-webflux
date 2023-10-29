package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.stream.IntStream;

@Slf4j
public class Cache {

    /**
     * [8]
     *
     * cache 연산자에 대해 알아본다.
     *
     * public final Flux<T> cache()
     * - 처음 subscribe 에서만 publisher 작업을 실행한다. (발생된 이벤트를 내부에 저장해놓는다.)
     * - 그 이후.. subscribe 를 한번 더하면 저장된 이벤트를 순서대로 흘려보낸다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux<Object> flux = Flux.create(
                sink -> {

                    IntStream.range(0, 3)
                            .forEach(
                                    i -> {
                                        log.info("publisher next value: {}, tx: {}", i, Thread.currentThread().getName());
                                        sink.next(i);
                                    }
                            );

                    log.info("publisher complete, tx: {}", Thread.currentThread().getName());
                    sink.complete();
                }
        ).cache(); // cache!

        flux.subscribe( // 첫번째 subscribe 에서는 publisher 작업을 실행한다.
                value -> log.info("first subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                null,
                () -> log.info("first subscribe complete, tx: {}", Thread.currentThread().getName())
        );

        flux.subscribe( // 두번째 subscribe 에서는 publisher 작업을 실행하지 않고 저장된 이벤트를 순서대로 받기만한다.
                value -> log.info("second subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                null,
                () -> log.info("second subscribe complete, tx: {}", Thread.currentThread().getName())
        );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
