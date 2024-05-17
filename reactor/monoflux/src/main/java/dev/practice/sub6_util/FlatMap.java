package dev.practice.sub6_util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class FlatMap {

    /**
     * [3]
     *
     * flatMap 연산자에 대해 알아본다.
     * public final <R> Flux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper)
     *
     * - onNext 이벤트를 받아서 publisher 를 반환한다.
     * - value -> [flatMap -> publisher] -> value & event
     * - 중첩 for loop 비슷한 결과가 나타난다.
     * - onErrorResume 과 구조가 비슷하다. (Throwable 을 받아 publisher 반환)
     *
     * 더 알아보기..
     * - thenMany, transform 연산자..
     *
     * 특이점
     * - TODO, 가끔! publishOn 에 의해 doOnNext 는 전부 parallel 로 동작하는데.. 마지막 (5,1), (5,2) 는 main 으로 동작한다.... 이유는?
     *
     * 참고
     * Mono 의 flatMap 은..
     * public final <R> Mono<R> flatMap(Function<? super T, ? extends Mono<? extends R>> transformer)
     * 로.. Flux 의 flatMap 과 다르게 리턴 publisher 가 Mono 로 한정적이다..
     *
     * 참고
     * Mono 에서 Flux 로 변환이 필요하다면..
     * thenMany (Mono 의 값은 사용불가능)
     * flatMapMany, expand, concatMap 을 사용할 수 있다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.range(1, 5)
                .flatMap( // onNext 가 전달되면 publisher 가 반환된다.
                        value -> Flux.range(1, 2)
                                .map(
                                        value2 -> {
                                            log.info("flatMap publisher run, from: {}, to: {}, tx: {}", value, value2, Thread.currentThread().getName());
                                            return "from: "+ value + ", to: " + value2;
                                        }
                                )
                                .publishOn(
                                        Schedulers.parallel()
                                )
                )
                .doOnNext(
                        value -> log.info("doOnNext: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe();

        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(1000); // 캐싱 스케줄러는 데몬 스레드이다.

    }
}
