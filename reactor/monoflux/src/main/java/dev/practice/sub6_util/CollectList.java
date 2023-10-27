package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class CollectList {

    /**
     * [7]
     *
     * collectList 연산자를 알아본다.
     *
     * public final Mono<List<T>> collectList()
     * - onNext 이벤트가 전달되면 자체적으로 내부에 item 을 적재한다. (downstream 으로 전달하지 않음)
     * - onComplete 이벤트가 전달되면 저장했던 item 들을 list 형태로 만들고 downstream 으로 onNext 와 onComplete 이벤트를 전달한다.
     * - Flux 를 Mono 로 바꿀때 유용하게 사용할 수 있다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.range(1, 5)
                .doOnNext(
                        value -> log.info("before collectList doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .collectList() // Mono 가 리턴 타입이다.
                .doOnNext(
                        value -> log.info("after collectList doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe();

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
