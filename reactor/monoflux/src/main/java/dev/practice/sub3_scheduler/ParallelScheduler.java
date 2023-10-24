package dev.practice.sub3_scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class ParallelScheduler {

    /**
     * [4]
     *
     * ParallelScheduler
     * - 캐싱된 n 개 크기의 스레드 풀을 제공한다. (ExecutorService 의 newFixedThreadPool 과 비슷한데 캐싱되어있으므로 생성을 따로 할 필요는 없는 느낌이다.)
     * - n 은 CPU 코어 수 이다.
     *
     * SingleScheduler 와 비교하여 하나이던 작업 스레드가 n 개로 늘어난 것 뿐.. 별 다를건 없다.
     *
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());


        for (int i = 0; i < 100; i++) {

            final int idx = i;

            Flux.create(
                    // subscribeOn + parallel 스케줄러를 적용하였으므로
                    // publisher 작업은 ParallelScheduler 에서 실행된다.
                    sink -> {
                        // 복습. Flux 의 create 로 sink 는 FluxSink 가 사용된다.
                        // 따라서 여러 스레드에서 동시에 비동기적으로 이벤트(next)를 전달할 수 있는 것이다.
                        log.info("publisher next: {}, tx: {}", idx, Thread.currentThread().getName());
                        sink.next(idx);
                    }
            ).subscribeOn(
                    Schedulers.parallel() // n 은 현재 8 로.. CPU 코어 수 임을 확인
            ).subscribe(
                    // subscribe 작업은 publisher 가 호출하므로 ParallelScheduler 에서 실행된다.
                    value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName())
            );
        }

        // 결과적으로 main 은 subscribe 를 거의 동시에 100 번을 호출하고 종료 되며..
        // ParallelScheduler 가 100개의 publisher, subscriber 작업을 순차적으로 수행하게 된다.

        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(2000); // Schedulder 에 사용된 스레드는 데몬 스레드 이므로 main 이 종료 되지 않게 잡아둔다.
    }
}
