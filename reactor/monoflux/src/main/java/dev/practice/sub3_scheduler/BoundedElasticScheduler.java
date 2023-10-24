package dev.practice.sub3_scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class BoundedElasticScheduler {

    /**
     * [5]
     * <p>
     * BoundedElasticScheduler
     * - "가변적인" 크기의 캐싱된 스레드 풀을 제공한다. (동적으로 스레드 관리, 캐싱되어있으므로 생성을 따로 할 필요는 없는 느낌이다.)
     * - 재사용 할 수 있는 스레드가 있다면 사용 하고, 없으면 새로 "생성"한다.
     * - 특정 시간(기본 60초) 동안 사용되지 않는 스레드면 제거된다.
     * - 생성 가능한 스레드 수는 CPU core 수 * 10 개로 제한된다. (out of memory 안전 장치)
     * - IO blocking 작업을 수행할 때 적합하다. (TODO 생각해보기..)
     */

    @SneakyThrows
    public static void main(String[] args) {


        log.info("start main, tx: {}", Thread.currentThread().getName());

        for (int i = 0; i < 200; i++) {

            final int idx = i;

            Flux.create(
                    // BoundedElasticScheduler 가 수행
                    sink -> {
                        log.info("publisher next: {}, tx: {}", idx, Thread.currentThread().getName());
                        sink.next(idx);
                    }
            ).subscribeOn(
                    Schedulers.boundedElastic() // BoundedElasticScheduler 사용, 가변 스레드 풀
            ).subscribe(
                    // BoundedElasticScheduler 가 수행
                    value -> log.info("subscriber value: {}, tx: {}", value, Thread.currentThread().getName())
            );
        }

        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(1000); // Schedulder 에 사용된 스레드는 데몬 스레드 이므로 main 이 종료 되지 않게 잡아둔다.
    }
}
