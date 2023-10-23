package dev.practice.sub2_sequence;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.stream.IntStream;

@Slf4j
public class FluxHandle {

    /**
     * [9]
     * <p>
     * Flux<T> 가 제공하는 handle 메서드에 대해 알아본다.
     * <p>
     * 독립적으로 sequence 를 생성하는 것은 아니다.
     * 존재하는 Source 의 흐름을 조작을 할 수 있게 도와주는 메서드이다.
     *
     * public final <R> Flux<R> handle(BiConsumer<? super T, SynchronousSink<R>> handler)
     *
     * 인자로 BiConsumer<? super T, SynchronousSink<R>> 를 전달한다.
     * - T: 현재 값(상태)이 전달된다.
     * - SynchronousSink<R> : sink 를 이용해서 현재 값을 조작 하거나 이벤트를 명시적으로 전달할 수 있다.
     *
     * 보통 interceptor 역할로 사용된다.
     */

    public static void main(String[] args) {


        log.info("start main, tx: {}", Thread.currentThread().getName());



        Flux.fromStream(IntStream.range(0, 10).boxed()) // source 는 이미 존재한다. 0 ~ 9 의 숫자가 item 으로 흐른다.
                .handle( // handle 로 source 흐름을 조작
                        (value, sink) -> { // BiConsumer<Integer, SynchronousSink<Object>>

                            if (value % 2 == 0) {
                                log.info("handle next: {}, tx: {}", value, Thread.currentThread().getName());
                                sink.next(value); // item 이 짝수이면 onNext 로 전달
                            } else if (value == 7) {
                                log.info("handle complete, tx: {}", Thread.currentThread().getName());
                                sink.complete(); // item 이 7 이면 onComplete 전달
                            }
                        }
                ).subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()), // 0, 2, 4, 6
                        error -> log.error("subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("subscribe complete, tx: {}", Thread.currentThread().getName())
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
