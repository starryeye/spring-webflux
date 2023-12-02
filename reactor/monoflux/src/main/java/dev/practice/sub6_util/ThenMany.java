package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.stream.Stream;

@Slf4j
public class ThenMany {

    /**
     * [13]
     * thenMany 연산자에 대해 알아본다.
     *
     * - 주로 Mono 에서 사용된다. (1 -> N)
     * - source 에서 downstream 으로 흘러오는 element 들은 모두 무시하고..
     * 완료 처리가 되면 파라미터로 가지고 있던 flux 를 play 한다.
     * - Flux 에서 사용하면 source 가 "완료 되면" 다른 publisher(mono, flux) 가 play 되도록 할 수 있다.
     * Flux 의 then 연산자의 상위 호환 인듯(then 을 사용시 Mono 만 넣어줄 수 있음)
     *
     * 참고
     * flatMap 과 헷갈릴 수 있는데..
     * flatMap 은 파라미터가 함수형인터페이스 Function 을 받고..
     * thenMany 의 파라미터는 publisher 이다.
     * 즉, thenMany 는 "완료 처리가 되면" 파라미터로 가지고 있던 pulbisher 를 play 하는 것이고..
     * flatMap 은 하나의 element 만 흘러와도 내부 Function 을 실행한다.
     */

    public static void main(String[] args) {
        log.info("start main, tx: {}", Thread.currentThread().getName());

        Mono.just("mono")
                .doOnNext(
                        value -> log.info("doOnNext, value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .doOnTerminate( // Mono 에는 doOnComplete 가 없다. 대체 연산자들이 많기 때문이다. doOnTerminate 는 완료 or 에러 시 실행되는 연산자이다.
                        () -> log.info("doOnTerminate, tx: {}", Thread.currentThread().getName())
                )
                .thenMany( // thenMany
                        Flux.fromStream(
                                Stream.of(1, 2, 3)
                        )
                )
                .subscribe(
                        value -> log.info("subscribe, value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("subscribe, error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("subscribe, complete, tx: {}", Thread.currentThread().getName()),
                        Context.empty()
                );


        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
