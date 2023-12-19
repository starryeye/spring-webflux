package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
public class Transform {

    /**
     * [14]
     * transform 연산자에 대해 알아본다.
     * - 해당 연산자는 Mono, Flux 모두 지원된다.
     *
     * - transform 연산자를 호출한 publisher (source publisher) 는 실행되지 않고..
     * 연산자 파라미터로 전달된 Function 함수형 인터페이스의 반환 Publisher 가 수행된다.
     *
     * - 한마디로 원본 source publisher 를 새로운 publisher 로 전환 시킬 수 있다.
     * 전환은 오로지 사용자 몫이다..
     *
     *
     * thenMany() 와 차이점
     * - thenMany 는 source publisher 의 이벤트가 무시되기는 하지만 downstream 으로 전달이 되긴하지만..
     * transform 은 수행자체가 안된다. (Function 함수형인터페이스 내부 구현에서 source publisher 를 play 시키면 동일한 동작으로 구현가능)
     *
     * flatMap() 과 차이점
     * - thenMany 와 마찬가지로 Function 함수형인터페이스 구현에 따라 transform 연산자를 사용하면
     * flatMap 과 동일하게 동작하도록 만들 수 있다.
     *
     *
     * 주의사항
     * - transform 연산자는 publisher 의 이벤트와는 전혀 상관이 없다.
     * publisher 를 이벤트와 상관없이 통째로 바꿀 수 있는 개념
     * - thenMany 에서는 source publisher 에서 complete 이벤트에 의해 수행되고.. (error 발생 시 thenMany 를 수행하지 않고 그대로 흘려보냄)
     * - flatMap 에서는 source publisher 에서 next 이벤트에 의해 수행되고.. (error 발생 시 thenMany 를 수행하지 않고 그대로 흘려보냄)
     *
     */

    public static void main(String[] args) {

        // Function<T, R> : T -> R
        Function<Flux<Integer>, Flux<String>> transformer =
                integerFlux -> {

                    // 여기서 Flux Integer 를 play 시킬 수 있긴 하다...
                    // - play 완료 되고 해당 결과로 publisher 를 생성하여 리턴을 시키던가
                    // - 혹은, integerFlux(T) 를 그대로 R 로 반환하여 기존의 원본 source publisher 를 그대로 play 하게끔 해볼 수 도 있겠다..

                    List<String> list = List.of("a", "b", "c");

                    return Flux.fromIterable(list);
                };


        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.fromStream(Stream.of(1, 2, 3))
                .doOnNext(
                        value -> log.info("Flux Integer, doOnNext: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .doOnComplete(
                        () -> log.info("Flux Integer, doOnComplete tx: {}", Thread.currentThread().getName())
                )
                .transform(transformer) // transform !
                .doOnNext(
                        value -> log.info("Flux String, doOnNext: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .doOnComplete(
                        () -> log.info("Flux String, doOnComplete tx: {}", Thread.currentThread().getName())
                )
                .subscribe();

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
