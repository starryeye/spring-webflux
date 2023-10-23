package dev.practice.sub2_sequence;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class FluxFrom {

    /**
     * [5]
     * <p>
     * Flux 가 제공하는 from* 메서드에 대해 알아본다.
     */

    public static void main(String[] args) {


        log.info("start main tx: {}", Thread.currentThread().getName());


        /**
         * fromIterable 메서드
         * public static <T> Flux<T> fromIterable(Iterable<? extends T> it)
         *
         * Iterable 을 인자로 전달한다.
         * Iterable 의 각 item 을 onNext 로 전달한다.
         */
        Flux.fromIterable(
                List.of(1, 2, 3, 4, 5)
        ).subscribe(
                value -> log.info("fromIterable value: {}, tx: {}", value, Thread.currentThread().getName())
        );


        /**
         * fromStream 메서드
         * public static <T> Flux<T> fromStream(Stream<? extends T> s)
         *
         * Stream 을 인자로 전달한다.
         * Stream 의 각 item 을 onNext 로 전달한다.
         *
         * 즉, Stream 을 Flux 로 합성할 수 있다.
         * Stream 그 자체를 넘기면 되기 때문에, Stream 의 종료 오퍼레이션은 필요 없다.
         */
        Flux.fromStream(
                IntStream.range(1, 6).boxed()
        ).subscribe(
                value -> log.info("fromStream value: {}, tx: {}", value, Thread.currentThread().getName())
        );


        /**
         * fromArray 메서드
         * public static <T> Flux<T> fromArray(T[] array)
         *
         * 배열을 인자로 전달한다.
         * 배열의 각 item 을 onNext 로 전달한다.
         */
        Flux.fromArray(
                new Integer[]{1, 2, 3, 4, 5}
        ).subscribe(
                value -> log.info("fromArray value: {}, tx: {}", value, Thread.currentThread().getName())
        );


        /**
         * range 메서드
         * public static Flux<Integer> range(int start, int count)
         *
         * start 부터 시작해서 1 씩 커진 값을 n 개 만큼 onNext 로 전달한다.
         */
        Flux.range(1, 5)
                .subscribe(
                        value -> log.info("range value: {}, tx: {}", value, Thread.currentThread().getName())
                );


        log.info("end main tx: {}", Thread.currentThread().getName());
    }
}
