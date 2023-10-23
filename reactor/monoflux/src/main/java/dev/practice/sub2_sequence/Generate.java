package dev.practice.sub2_sequence;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class Generate {

    /**
     * [6]
     *
     * Flux 가 제공하는 generate 메서드에 대해 알아본다.
     */

    public static void main(String[] args) {

        log.info("start main");

        /**
         * public static <T, S> Flux<T> generate(
         *                  Callable<S> stateSupplier,
         *                  BiFunction<S, SynchronousSink<T>, S> generator
         *                  )
         *
         * - 동기적으로 Flux 를 생성한다.
         * - 여기서의 state 는 특별하게 생각하지말고 value 로 보면 된다.
         *
         * 인자
         * - stateSupplier : 초기 값을 제공하는 Callable 구현체
         * - generator : 순서대로 왼쪽 두개의 타입 파라미터로 하나의 결과(세번째 타입파라미터)를 만들어 내는 BiFunction 구현체
         * - - 첫번째 S: state, 현재 값(state)이다.
         * - - SynchronousSink<T> : 명시적으로 next, error, complete 이벤트를 전달할 수 있다.
         * - - 두번째 S: 다음 순회에 전달될 값(state) 이다.
         *
         * 특징
         * - 한번의 generator 로 최대 한번의 next 호출 가능
         * - 만약 연속으로 두번 next 를 호출하면 error 이벤트가 발생하고 흐름이 종료된다.
         *
         */
        Flux.generate(
                () -> 0, // Callable<Integer> 초기 값(상태) 제공
                (state, sink) -> { // BiFunctional<Integer, SynchronousSink<Object>, Integer>

                    sink.next(state); // 현재 값(상태)를 next 로 전달

                    log.info("generate current state: {}, tx: {}", state, Thread.currentThread().getName());

                    if (state == 9) { // 종료 조건
                        sink.complete(); // complete 이벤트 전달
                    }

                    return state + 1; // 다음 순회에 전달될 값(상태) 을 반환
                }
        ).subscribe(
                value -> log.info("value: {}, tx: {}", value, Thread.currentThread().getName()),
                error -> log.error("error: {}, tx: {}", error, Thread.currentThread().getName()),
                () -> log.info("complete, tx: {}", Thread.currentThread().getName())
        );
    }
}
