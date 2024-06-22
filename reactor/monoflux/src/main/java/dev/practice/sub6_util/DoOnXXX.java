package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class DoOnXXX {

    /**
     * [2]
     *
     * doOn* 연산자에 대해 알아본다.
     *
     * - doOnSubscribe
     * public final Flux<T> doOnSubscribe(Consumer<? super Subscription> onSubscribe)
     *
     * - doOnNext
     * public final Flux<T> doOnNext(Consumer<? super T> onNext)
     *
     * - doOnComplete
     * public final Flux<T> doOnComplete(Runnable onComplete)
     *
     * - doOnError
     * public final Flux<T> doOnError(Consumer<? super Throwable> onError)
     *
     * - doOnRequest
     * public final Flux<T> doOnRequest(LongConsumer consumer)
     *
     * - 이 외에도 많음
     *
     * doOn* 연산자들은..
     * - 각각의 이벤트를 흐름에 영향을 주지 않고.. 위에서 내려오는 이벤트에 대해 추가 작업이 가능하다.
     * - 즉, subscribe 인것 처럼 파이프라인 도중에 해당 이벤트가 내려오면 매칭되는 연산자의 함수가 동작한다.
     *
     * todo, doOnEach 연산자는 Signal 을 consume 하면서.. 여러 이벤트를 한꺼번에 다룰수 있다..
     *  그렇다면.. downstream 방향으로 연산자 순서에 영향을 받나?
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.range(1, 5)
                .map(
                        value -> value * 2
                )
                .doOnNext( // onNext 이벤트가 파이프라인에 도착하면 해당 함수를 실행한다.
                        value -> log.info("doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .doOnComplete( // onComplete 이벤트가 파이프라인에 도착하면 해당 함수를 실행한다.
                        () -> log.info("doOnComplete tx: {}", Thread.currentThread().getName())
                )
                .doOnSubscribe( // onSubscribe 이벤트가 파이프라인에 도착하면 해당 함수를 실행한다.
                        // subscription 이 전달됨
                        subscription -> log.info("doOnSubscribe subscription: {}, tx: {}", subscription, Thread.currentThread().getName())
                )
                .doOnRequest( // onRequest 이벤트가 파이프라인에 도착하면 해당 함수를 실행한다.
                        // subscriber 가 publisher 에 요청한 요청 개수 (back pressure)
                        // 기본 값은 unbounded request, Long.MAX_VALUE 이다.
                        value -> log.info("doOnRequest value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .map(
                        value -> value / 2
                )
                .subscribe();

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }

}
