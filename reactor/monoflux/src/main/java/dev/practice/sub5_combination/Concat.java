package dev.practice.sub5_combination;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
public class Concat {

    /**
     *
     * [2]
     *
     * concat 연산자
     * - publisher 를 다른 publisher 와 합치는 연산자이다.
     * - 두 publisher 가 전달하는 onNext 이벤트가 하나의 downstream(파이프라인) 을 통해 전달된다.
     * - 앞선 publisher 가 onComplete 이벤트를 전달하면 그 다음 pulisher 가 subscribe 시작한다.
     * - 즉, 동일한 파이프라인으로 publisher 하나가 끝나면 그다음 publisher 가 시작하고 끝내는 방식이다.
     * - 따라서, 순서가 보장된다.
     *
     *
     * 특이점
     * - 첫번째 publisher 의 onComplete 이벤트는 subscriber 에게 전달되지 않는다. (생각해보면 전달되면 안됨)
     * - 로그를 보면 ParallelScheduler 가 사용되는데.. 이는 delayElements 연산자 때문이다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux<Integer> flux1 = Flux.range(1, 3)
                .doOnSubscribe(
                        value -> log.info("flux 1 subscribe start")
                )
                .delayElements(
                        Duration.ofMillis(100)
                );

        Flux<Integer> flux2 = Flux.range(10, 3)
                .doOnSubscribe(
                        value -> log.info("flux 2 subscribe start")
                )
                .delayElements(
                        Duration.ofMillis(100)
                );

        Flux.concat(flux1, flux2) // 두 개의 publisher 를 concat 연산자로 결합
                .doOnNext(
                        value -> log.info("concat doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe(
                        value -> log.info("concat subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("concat subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("concat subscribe complete, tx: {}", Thread.currentThread().getName())
                );


        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(2000);// 캐싱 스레드 (parallel) 종료 방지용
    }
}
