package dev.practice.sub6_util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class Skip {

    /**
     * [6]
     *
     * skip, skipLast 연산자를 알아본다.
     *
     * skip
     * public final Flux<T> skip(long skipped)
     * - 처음 n 개의 onNext 이벤트를 무시하고 그 이후 onNext 이벤트를 전파한다.
     *
     * skipLast
     * public final Flux<T> skipLast(int n)
     * - onComplete 이벤트가 발생하기 직전 n 개의 onNext 이벤트를 무시한다.
     * - takeLast 처럼, 전체 value 개수를 알아야하므로 내부에 배열로 적재하면서 흐름을 만들어내는것 같다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.range(1, 10)
                .doOnNext( // skip 으로 전달되기 전의 로깅이므로, 모든 value 로깅됨
                        value -> log.info("before skip, doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .skip(5) // skip
                .doOnNext( // 6, 7, 8, 9, 10 로깅
                        value -> log.info("after skip, doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe();

        Flux.range(1, 10)
                .doOnNext( // skipLast 로 전달되기 전의 로깅이므로, 모든 value 로깅됨
                        value -> log.info("before skipLast, doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .skipLast(5) // skipLast
                .doOnNext( // 1, 2, 3, 4, 5 로깅
                        value -> log.info("after skipLast, doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe();


        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
