package dev.practice.sub3_scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class SingleScheduler {

    /**
     * [3]
     *
     * SingleScheduler
     * - 캐싱된 1 개 크기의 스레드 풀을 제공한다.
     * - 모든 publisher 작업, subscribe 작업이 하나의 스레드에서 실행된다. (ExecutorService 의 newSingleThreadExecutor 와 비슷한데.. 캐싱되어있으므로 생성을 따로 할 필요는 없는 느낌이다.)
     *
     *
     * 아래 코드의 주석에 숫자를 달아 놨다 순서대로 보며 느껴보자 ex. (1), (2) ...
     */


    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        for (int i = 0; i < 100; i++) {

            final int idx = i;

            Flux.create(
                    // (1), (2) 에 의하여 publisher(source) 의 작업은 SingleScheduler 에서 실행된다.... (3)
                    sink -> {
                        log.info("publisher next: {}, tx: {}", idx, Thread.currentThread().getName());
                        sink.next(idx);
                    }
            ).subscribeOn( // subscribeOn 은 source(publisher) 의 실행 스레드에 영향을 준다... (1)
                    Schedulers.single() // SingleScheduler 를 사용... (2)
            ).subscribe(
                    // main 스레드 에서 subscribe 를 호출 하였지만 subscribeOn 에 SingleScheduler 를 사용한다고 하였으므로..
                    // publisher, subscriber 의 작업은 main 스레드에서 실행되지 않는다.
                    // 즉, subscribe 호출 후 바로 다음 line(for loop 를 계속 돈다.) 으로 실행 코드가 넘어감

                    // publisher 에서 onNext 를 호출하므로, subscribe 작업 역시 SingleScheduler 에서 수행된다.... (4)
                    value -> log.info("subscriber value: {}, tx: {}", value, Thread.currentThread().getName())
            );
        }
        // 결과적으로 main 은 subscribe 를 거의 동시에 100 번을 호출하고 종료 되며..
        // SingleScheduler 가 100개의 publisher, subscriber 작업을 순차적으로 수행하게 된다.

        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(1000); // Schedulder 에 사용된 스레드는 데몬 스레드 이므로 main 이 종료 되지 않게 잡아둔다.
    }
}
