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
     * 완료 처리(onComplete 이벤념)가 되면 파라미터로 가지고 있던 flux 를 play 한다.
     * - Flux 에서 사용하면 source 가 "완료 되면" 다른 publisher(mono, flux) 가 play 되도록 할 수 있다.
     * Flux 의 then 연산자의 상위 호환 인듯(then 을 사용시 Mono 만 넣어줄 수 있음)
     *
     * 참고
     * flatMap 과 헷갈릴 수 있는데..
     * flatMap 은 파라미터가 함수형인터페이스 Function 을 받고..
     * thenMany 의 파라미터는 publisher 이다.
     * 즉, thenMany 는 "완료 처리가 되면" 파라미터로 가지고 있던 pulbisher 를 play 하는 것이고..
     * flatMap 은 하나의 element 만 흘러와도 내부 Function 을 실행한다.
     *
     * 주의사항
     * - flatMap 처럼, source 에서 error 이벤트가 전달되면 thenMany 는 수행하지 않고, error 가 downstream 으로 전달됨
     *
     * 더 알아보기..
     * - transform 연산자
     */

    public static void main(String[] args) {
        log.info("[normal case] start main, tx: {}", Thread.currentThread().getName());

        // 정상 케이스
        Mono.just("mono")
                .doOnNext(
                        value -> log.info("[normal case] doOnNext, value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .doOnTerminate( // Mono 에는 doOnComplete 가 없다. 대체 연산자들이 많기 때문이다. doOnTerminate 는 완료 or 에러 시 실행되는 연산자이다.
                        () -> log.info("[normal case] doOnTerminate, tx: {}", Thread.currentThread().getName())
                )
                .thenMany( // thenMany
                        Flux.fromStream(
                                Stream.of(1, 2, 3)
                        )
                )
                .doOnNext(
                        value -> log.info("[normal case] doOnNext, value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe(
                        value -> log.info("[normal case] subscribe, value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("[normal case] subscribe, error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("[normal case] subscribe, complete, tx: {}", Thread.currentThread().getName()),
                        Context.empty()
                );

        log.info("[normal case] end main, tx: {}", Thread.currentThread().getName());


        log.info("[error case] start main, tx: {}", Thread.currentThread().getName());


        // error 케이스
        Mono.error(new RuntimeException())
                .doOnTerminate(
                        () -> log.info("[error case] doOnTerminate, tx: {}", Thread.currentThread().getName())
                )
                .thenMany(
                        Flux.fromStream(
                                Stream.of(1, 2, 3)
                        )
                )
                .doOnNext(
                        value -> log.info("[error case] doOnNext, value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe(
                        value -> log.info("[error case] subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("[error case] subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("[error case] subscribe complete, tx: {}", Thread.currentThread().getName())
                );


        log.info("[error case] end main, tx: {}", Thread.currentThread().getName());
    }
}
