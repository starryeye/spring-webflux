package dev.practice.sub5_combination;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
public class DelayElements {

    /**
     * [1]
     * <p>
     * delayElements 연산자..
     *
     * - 생성된 소스 publisher 에 붙일 수 있는 연산자이다.
     * - 인자로 전달된 시간 만큼의 간격을 두고 onNext 이벤트를 전달한다.
     *
     * 특이점..
     * - delayElements 이후 downstream 부터 delay 가 걸리며.. ParallelScheduler 가 사용된다.
     * - TODO 그러나.. 첫번째 doOnNext 에만 single 이 사용되고.. 이후 doOnNext 에는 ParallelScheduler 가 사용된다... 뭐야..
     * ->>> 개인 예상) delayElements 에 사용되는 스레드 풀은 파이프라인 순서에 상관없이.. source 에서 이벤트 발행 직후 영향을 끼치는듯 (최초 delayElements 연산자 도달까지는 제외)
     * ->>> 마치 source 바로 밑에 달린 연산자 처럼 행동..하는듯..
     *
     * 참고
     * delay 보다 더 오랜시간이 걸려서 onNext 이벤트가 전달되면 그 아이템은 바로 전달된다
     * -> 즉, 시간의 간격 값은 최소로 지키는 값이다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.create(
                        sink -> {
                            for (int i = 1; i <= 5; i++) {

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                log.info("publisher item: {}, tx: {}", i, Thread.currentThread().getName());
                                sink.next(i); //0.1초 간격으로 1 부터 5 까지 onNext 전달
                            }

                            sink.complete();
                        }
                )
                .doOnNext(
                        value -> log.info("doOnNext1 value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .delayElements(
                        Duration.ofMillis(1000) // 0.5 초 간격으로 onNext 이벤트 전달하도록 delay 설정
                )
                .doOnNext(
                        value -> log.info("doOnNext2 value: {}, tx: {}", value, Thread.currentThread().getName()) // ParallelScheduler 로 실행된다...
                )
                .subscribeOn(
                        Schedulers.single() // SingleScheduler 로 publisher 작업 실행 스레드 설정
                )
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("subscribe complete, tx: {}", Thread.currentThread().getName())
                );




        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(6000); // 캐싱 스레드는 데몬 스레드라 대기
    }
}
