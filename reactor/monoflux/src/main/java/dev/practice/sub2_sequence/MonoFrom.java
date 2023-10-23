package dev.practice.sub2_sequence;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class MonoFrom {

    /**
     * [4]
     *
     * Mono 가 제공하는 from* 메서드를 알아본다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        /**
         * fromCallable 메서드
         * public static <T> Mono<T> fromCallable(Callable<? extends T> supplier)
         *
         * Callable 함수형 인터페이스의 구현체를 인자로 받는다.
         * Callable 을 실행하고 반환값을 onNext 로 전달한다.
         */
        Mono.fromCallable(
                () -> {
                    log.info("fromCallable tx: {}", Thread.currentThread().getName());
                    return 1;
                }
        ).subscribe(
                value -> log.info("fromCallable value: {} , tx: {}", value, Thread.currentThread().getName())
        );


        /**
         * fromFuture 메서드
         * public static <T> Mono<T> fromFuture(CompletableFuture<? extends T> future)
         *
         * CompletableFuture 를 인자로 전달한다.
         * CompletableFuture 가 done 상태가 되면 반환값을 onNext 로 전달한다.
         *
         * 즉, CompletableFuture 와 Mono 를 합성할 수 있는 것이다.
         */
        Mono.fromFuture(
                CompletableFuture.supplyAsync(
                        () -> {
                            log.info("fromFuture tx: {}", Thread.currentThread().getName());
                            return 1;
                        }
                )
        ).subscribe(
                value -> log.info("fromFuture value: {} , tx: {}", value, Thread.currentThread().getName())
        );


        /**
         * fromSupplier
         * public static <T> Mono<T> fromSupplier(Supplier<? extends T> supplier)
         *
         * Supplier 함수형 인터페이스의 구현체를 인자로 전달한다.
         * Supplier 를 실행하고 반환값을 onNext 로 전달한다.
         */
        Mono.fromSupplier(
                () -> {
                    log.info("fromSupplier tx: {}", Thread.currentThread().getName());
                    return 1;
                }
        ).subscribe(
                value -> log.info("fromFuture value: {} , tx: {}", value, Thread.currentThread().getName())
        );


        /**
         * fromRunnable
         * public static <T> Mono<T> fromRunnable(Runnable runnable)
         *
         * Runnable 함수형 인터페이스의 구현체를 인자로 전달한다.
         * Runnable 을 실행하고 끝나면 onComplete 를 전달한다.
         */
        Mono.fromRunnable(
                () -> log.info("fromRunnable tx: {}", Thread.currentThread().getName())
        ).subscribe(
                null,
                null,
                () -> log.info("fromRunnable complete tx:{}", Thread.currentThread().getName())
        );


        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
