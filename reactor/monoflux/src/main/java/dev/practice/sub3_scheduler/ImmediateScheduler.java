package dev.practice.sub3_scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class ImmediateScheduler {

    /**
     * [2]
     *
     * Scheduler
     * - publisher 혹은 subscribe 가 어떤 scheduler 를 활용했냐에 따라
     * 해당 작업이 실행되는 스레드 풀이 결정된다.
     * - ImmediateScheduler, SingleScheduler, ParallelScheduler, BoundedElasticScheduler 가 존재한다.
     *
     * ImmediateScheduler
     * - subscribe 를 호출한 caller 스레드에서 즉시 실행한다.
     * - 별도로 Schedulers 를 지정하지 않으면 사용된다. (즉, 기본 값)
     *
     * 아래 코드의 주석에 숫자를 달아 놨다 순서대로 보며 느껴보자 ex. (1), (2) ...
     */

    @SneakyThrows
    public static void main(String[] args) {


        // main 스레드 영역
        log.info("start main, tx: {}", Thread.currentThread().getName());


        Flux.create(
                // (2), (3) 에 따라서..
                // publisher 에서 수행할 작업(source 실행)은 main 에서 수행함... (4)
                sink -> {
                    for (int i = 1; i <= 5; i++) {
                        log.info("publisher next: {}, tx: {}", i, Thread.currentThread().getName());
                        sink.next(i);
                    }
                }
        ).subscribeOn( // subscribeOn 은 source 의 실행 스레드에 영향을 준다...(2)
                // Scheduler 로 ImmediateScheduler 를 사용 하여, subscriber 를 호출한 caller 스레드(main) 에서 즉시 수행한다....(3)
                Schedulers.immediate()
        ).subscribe( // Flux(publisher) 의 subscribe 메서드를 호출한 스레드는 현재 main 이다... (1)
                // publisher 에서 onNext 가 호출되어 subscriber 작업 또한 main 에서 수행된다... (5)
                value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName())
        );


        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
