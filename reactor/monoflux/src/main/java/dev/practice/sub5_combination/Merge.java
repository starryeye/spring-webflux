package dev.practice.sub5_combination;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
public class Merge {

    /**
     * [3]
     *
     * merge 연산자
     * - publisher 를 다른 publisher 와 결합하는 연산자이다.
     * - concat 과 다르게 모든 publisher 를 바로 subscribe 시작한다.
     * - 그래서 각 publisher 의 onNext 이벤트가 subscriber 로 마구잡이로 전달되고 순서도 보장되지 않는다.
     * - - 물론 publisher 하나씩 때고 보면 순서가 보장될것이다.
     *
     * 참고
     * - 하나의 스레드로 모든 작업을 실행하면
     * 당연히 코드 순서인 첫번째 publisher 작업을 모두 수행하고
     * 두번째 publisher 작업을 모두 수행하게 된다.
     * 현재 코드에서는 deleyElements 로 parallel 스케줄러를 사용하므로 원래 merge 의 의도로 사용할 수 있었다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux<Integer> flux1 = Flux.range(1, 3)
                .doOnSubscribe(
                        value -> log.info("flux 1 subscribe start, tx: {}", Thread.currentThread().getName())
                )
                .delayElements(
                        Duration.ofMillis(100)
                );

        Flux<Integer> flux2 = Flux.range(10, 3)
                .doOnSubscribe(
                        value -> log.info("flux 2 subscribe start, tx: {}", Thread.currentThread().getName())
                )
                .delayElements(
                        Duration.ofMillis(100)
                );

        Flux.merge(flux1, flux2) // merge 연산자로 두 개의 publisher 합성
                .doOnNext(
                        value -> log.info("merge doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe(
                        value -> log.info("merge subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("merge subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("merge subscribe complete, tx: {}", Thread.currentThread().getName())
                );


        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(2000); // 캐싱 스케줄러 종료 방지(데몬)
    }
}
