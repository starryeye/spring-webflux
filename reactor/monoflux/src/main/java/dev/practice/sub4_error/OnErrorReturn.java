package dev.practice.sub4_error;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.stream.IntStream;

@Slf4j
public class OnErrorReturn {

    /**
     * [3]
     *
     * onErrorReturn 연산자로 에러 처리
     *
     * - onError 이벤트를 처리하기 위해 파이프라인 중간에서
     * 고정된 값으로 변환하여 에러를 처리하는 방법이다.
     * - 고정된 값으로 전달(onNext) 이후, onComplete 이벤트 처리가 된다.
     *
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

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
                .onErrorReturn(5) // onErrorReturn 으로 에러를 고정 값(5, onNext)으로 처리하고 onComplete 처리 후 종료
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("subscribe complete")
                );

        // 1, 2, 3, 4, 5, complete

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
