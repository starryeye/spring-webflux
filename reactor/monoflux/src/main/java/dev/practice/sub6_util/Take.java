package dev.practice.sub6_util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class Take {

    /**
     * [5]
     *
     * take, takeLast 연산자에 대해 알아본다.
     *
     * take
     * - 처음 부터 n 개까지는 onNext 이벤트를 downstream 으로 이벤트를 전파하고..
     * - 이후엔 onComplete 이벤트를 발생시켜버린다.
     *
     * takeLast
     * - 마지막 n 개의 onNext 이벤트만 downstream 으로 이벤트를 전파하고 onComplete 이벤트 발생시킴
     * - 그 전의 onNext 이벤트는 전달하지 않는다.
     * - 내부에 n 크기의 배열에 value 를 적재 및 덮어쓰기 하다가.. onComplete 가 오면 그때 onNext 를 전달하는 방식으로 동작한다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.range(1, 10)
                .take(5) // take 5 적용
                .doOnNext(
                        value -> log.info("take value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe();

        Flux.range(1, 10)
                .takeLast(5) // takeLast 5 적용
                .doOnNext(
                        value -> log.info("takeLast value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe();


        log.info("end main, tx: {}", Thread.currentThread().getName());

    }
}
