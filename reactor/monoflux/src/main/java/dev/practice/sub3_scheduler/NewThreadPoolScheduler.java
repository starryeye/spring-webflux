package dev.practice.sub3_scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class NewThreadPoolScheduler {

    /**
     * [6]
     *
     * new*
     *
     * single, parallel, boundedElastic 는..
     * 모두 캐싱된 스레드 풀을 제공한다.
     *
     * newSingle, newParallel, newBounded 는 새로운 스레드 풀을 만들어 제공할 수 있다.
     * - 주의할 점은 dispose 로 스레드 풀 해제는 꼭 필요하다. (캐싱된 스레드 풀에서는 할 필요가 없었음)
     */

    @SneakyThrows
    public static void main(String[] args) {


        log.info("start main, tx: {}", Thread.currentThread().getName());

        for (int i = 0; i < 100; i++) {

            // 캐싱된 스레드 풀을 사용하지 않고 직접 스레드 풀을 만들어서 할당하기 위함
            Scheduler newSingle = Schedulers.newSingle("new single");

            final int idx = i;

            Flux.create(
                    // newSingle 로 작업이 실행된다.
                    sink -> {
                        log.info("publisher next: {}, tx: {}", idx, Thread.currentThread().getName());
                        sink.next(idx);
                    }
            ).subscribeOn(
                    newSingle // 직접 만든 스레드 풀을 사용
            ).subscribe(
                    value -> {
                        // newSingle 로 작업이 실행된다.
                        log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName());

                        newSingle.dispose(); // 스레드 풀 사용이 완료 되었으면 해제는 필수이다.
                    }
            );
        }

        // 동시에 100 개의 SingleScheduler(1개의 크기를 가진 스레드 풀) 가 생성되고
        // 각 스레드로 publisher, subscribe 작업이 수행되고 해제 된다.

        // 참고로 직접 만든 스레드는 데몬 스레드가 아니라서 main 이 종료되더라도 생성된 스레드의 작업은 진행된다.
        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
