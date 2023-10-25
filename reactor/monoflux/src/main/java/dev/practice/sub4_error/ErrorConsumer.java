package dev.practice.sub4_error;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.stream.IntStream;

@Slf4j
public class ErrorConsumer {

    /**
     * [2]
     *
     * subscribe 의 두 번째 인자인 errorConsumer 를 통해서 onError 이벤트를 처리한다.
     *
     * onError 이벤트 이후 파이프라인은 종료 되고 더이상 이용은 불가하다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

//        Flux.error(new RuntimeException("publisher error")) // publisher 의 error 메서드를 통해 onError 이벤트를 전달한다. (Throwable)
//                .subscribe(
//                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
//                        error -> log.info("subscribe error: {}, tx: {}", error, Thread.currentThread().getName()), // 에러를 처리한다.
//                        () -> log.info("subscribe complete")
//                );

        Flux.create(
                    fluxSink -> {
                        IntStream.range(1, 10)
                                .forEach(
                                        i -> {
                                            if (i == 5) {
                                                fluxSink.error(new RuntimeException("publisher error"));
                                            }else {
                                                fluxSink.next(i);
                                            }
                                        }
                                );
                    }
                )
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("subscribe error: {}, tx: {}", error, Thread.currentThread().getName()), // 에러를 처리한다.
                        () -> log.info("subscribe complete")
                );

        // 1 ~ 4 까지 전달 되고 onError 이벤트가 발생한다.
        // 이후로는 더이상 publisher 작업이 진행되지 않는다.
        // onComplete 이벤트도 발생하지 않는다.

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
