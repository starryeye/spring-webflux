package dev.practice.sub5_combination;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
public class MergeSequential {

    /**
     * [4]
     *
     * mergeSequential 연산자.
     * - publisher 를 다른 publisher 와 결합하는 연산자이다.
     * - merge 와 마찬가지로 모든 publisher 를 바로 subscribe 한다.
     * - merge 와 마찬가지로 각 publisher 의 onNext 이벤트가 마구잡이로 공통 subscriber 에게 전달된다.
     * - merge 와 다르게 내부에서 재정렬해서 publisher 간 순서를 보장한다.(onNext 이벤트 기준 concat 과 결과가 비슷하다.)
     * - merge 와 concat 의 특성을 섞은 느낌이다.
     *
     * 특이점..
     * - flux 1 과 flux 2 의 doOnNext 는.. (delayElements 이전)
     * ->> 왜.. 일부는 main 에서 수행되고 일부는 parallel 이지..? 모두 main 이어야 할 것 같음.. -> TODO DelayElements 의문점과 동일
     * - flux 1 과 flux 2 의 delayElements.. 이후 와 mergeSequential 이후 는 예상한 대로 동작하는듯..
     */

    @SneakyThrows
    public static void main(String[] args) {


        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux<Integer> flux1 = Flux.range(1, 3)
                .doOnSubscribe(
                        value -> log.info("flux 1 subscribe start, tx: {}", Thread.currentThread().getName())
                )
                .doOnNext(
                        value -> log.info("flux 1 doOnNext value : {}, tx: {}", value, Thread.currentThread().getName())
                )
                .delayElements(
                        Duration.ofMillis(100) // 0.1 초 지연, parallelScheduler 사용
                );

        Flux<Integer> flux2 = Flux.range(10, 3)
                .doOnSubscribe(
                        value -> log.info("flux 2 subscribe start, tx: {}", Thread.currentThread().getName())
                )
                .doOnNext(
                        value -> log.info("flux 2 doOnNext value : {}, tx: {}", value, Thread.currentThread().getName())
                )
                .delayElements(
                        Duration.ofMillis(100) // 0.1 초 지연, parallelScheduler 사용
                );

        Flux.mergeSequential(flux1, flux2) // 두개의 publisher 를 mergeSequential 연산자로 결합
                .doOnNext(
                        value -> log.info("mergeSequential doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe();

        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(2000); // 캐싱 스레드 풀(Parallel 스케줄러, 데몬) 종료 방지
    }
}
