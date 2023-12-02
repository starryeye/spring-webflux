package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;

@Slf4j
public class Then {
    /**
     * [12]
     * then 연산자에 대해 알아본다.
     *
     * - Mono, Flux 에서 모두 사용할 수 있다.
     * - 기본 동작은 기존 source 에서 downstream 으로 흘러나오는 element 들은 모두 무시하고 onComplete 가 오면
     * 파라미터로 가지고 있는 Mono play 하던가 파라미터가 없다면 onComplete 를 그대로 흘려보낸다.
     * - doOnComplete 와의 차이점은 새로운 Mono 를 흘릴 수 있는 선택지가 존재한다는 점이다.
     *
     *
     * Mono
     * - public final Mono<Void> then()
     * source 에서 downstream 으로 흘러오는 값들을 모두 무시한다. onComplete 가 오면 그대로 흘린다.
     * - public final <V> Mono<V> then(Mono<V> other)
     * source 에서 downstream 으로 흘러오는 값들을 모두 무시하고 source 에서 onComplete 가 오면
     * 파라미터로 받은 Mono 를 play 한다.
     *
     * Flux
     * - public final Mono<Void> then()
     * Mono 와 기본 동작 동일
     * - public final <V> Mono<V> then(Mono<V> other)
     * Mono 와 기본 동작 동일
     */

    public static void main(String[] args) {

        monoThen("monoThen");
        fluxThen("fluxThen");
    }

    private static void monoThen(String name) {
        log.info("[{}] start main, tx: {}", name, Thread.currentThread().getName());

        Mono.just(List.of(1, 2, 3))
                .doOnNext(
                        value -> log.info("[{}] doOnNext, value: {}, tx: {}", name, value, Thread.currentThread().getName())
                )
                .then()
                .subscribe(
                        value -> log.info("[{}] subscribe, value: {}, tx: {}", name, value, Thread.currentThread().getName()),
                        error -> log.info("[{}] subscribe, error: {}, tx: {}", name, error, Thread.currentThread().getName()),
                        () -> log.info("[{}] subscribe, complete, tx: {}", name, Thread.currentThread().getName()),
                        Context.empty()
                );

        log.info("[{}] end main, tx: {}", name, Thread.currentThread().getName());
    }

    private static void fluxThen(String name) {

        log.info("[{}] start main, tx: {}", name, Thread.currentThread().getName());

        Flux.fromIterable(List.of(1, 2, 3))
                .doOnNext(
                        value -> log.info("[{}] doOnNext, value: {}, tx: {}", name, value, Thread.currentThread().getName())
                )
                .doOnComplete(
                        () -> log.info("[{}] doOnComplete, tx: {}", name, Thread.currentThread().getName())
                )
                .then(Mono.just(List.of(4, 5, 6)))
                .subscribe(
                        value -> log.info("[{}] subscribe, value: {}, tx: {}", name, value, Thread.currentThread().getName()),
                        error -> log.info("[{}] subscribe, error: {}, tx: {}", name, error, Thread.currentThread().getName()),
                        () -> log.info("[{}] subscribe, complete, tx: {}", name, Thread.currentThread().getName()),
                        Context.empty()
                );

        log.info("[{}] end main, tx: {}", name, Thread.currentThread().getName());
    }
}
