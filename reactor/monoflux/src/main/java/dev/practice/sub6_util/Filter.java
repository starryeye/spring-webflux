package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class Filter {

    /**
     * [4]
     *
     * filter 연산자에 대해 알아본다.
     * public final Flux<T> filter(Predicate<? super T> p)
     *
     * - onNext 이벤트를 받아서 boolean 을 반환한다.
     * - boolean 값이 true 라면 onNext 이벤트를 그대로 전달하고
     * - boolean 값이 false 라면 더이상 전파하지 않는다.
     * - Stream 의 filter 와 비슷
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.range(1, 5)
                .filter(
                        value -> value % 2 == 0 // 홀수면 필터링한다. 짝수만 통과시킨다.
                )
                .doOnNext(
                        value -> log.info("doOnNext value: {}", value, Thread.currentThread().getName())
                )
                .subscribe();

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
