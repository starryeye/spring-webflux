package dev.practice.sub3_scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.stream.IntStream;

@Slf4j
public class PublishOnSubscribeOn {

    /**
     * [9]
     *
     * publishOn, subscribeOn 혼합 사용 예시
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.create(
                sink -> {

                    // subscribeOn 에 의해 Source(publisher) 작업은 ParallelScheduler 가 실행한다.
                    for (var i = 0; i < 5; i++) {
                        log.info("publisher next: {}, tx: {}", i, Thread.currentThread().getName());
                        sink.next(i);
                    }
                }
        ).publishOn(
                Schedulers.single()
        ).doOnNext(
                // 위 publishOn 에 의해 SingleScheduler 로 작업이 실행된다.
                item -> log.info("doOnNext1 item: {}, tx: {}", item, Thread.currentThread().getName())
        ).publishOn(
                Schedulers.boundedElastic() // publishOn 이라서.. 스레드 풀에 스레드가 많아도 하나의 스레드만 작업함..
        ).doOnNext(
                // 위 publishOn 에 의해 BoundedElasticScheduler 로 작업이 실행된다.
                item -> log.info("doOnNext2 item: {}, tx: {}", item, Thread.currentThread().getName())
        ).subscribeOn(
                Schedulers.parallel()
        ).subscribe(
                // 위 파이프라인 연산자를 실행한 스레드(BoundedElasticScheduler)가 계속 실행한다.
                value -> log.info("value: " + value)
        );


        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(1000); // 캐시된 Scheduler 데몬 스레드 종료 방지
    }
}
