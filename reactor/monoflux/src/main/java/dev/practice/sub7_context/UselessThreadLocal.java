package dev.practice.sub7_context;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class UselessThreadLocal {

    /**
     * [1]
     *
     * ThreadLocal
     * - 여러개의 스레드가 존재할 때, 해당 스레드만 접근할 수 있는 특별한 저장소
     * - ThreadLocal 클래스는 오직 한 스레드에 의해 읽고 쓸 수 있는 변수를 생성한다.
     * - 멀티 스레드 환경에서 공유 자원의 동시성 문제를 해결
     *
     *
     * publishOn, subscribeOn 으로 실행 스레드가 달라질 경우..
     * ThreadLocal 을 쓸 수 있을까?
     * -> 아래 실행결과를 참고 해봐도 되지만.. 스레드로컬 사용할 수 없다. (스레드가 변경되므로..)
     * -> 그래서 나온게 "context" 이다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        // ThreadLocal 생성
        ThreadLocal<String> threadLocal = new ThreadLocal<>();

        threadLocal.set("starryeye");

        Flux.create(
                sink -> {
                    // SingleScheduler 로 실행
                    log.info("threadLocal: {}, tx: {}", threadLocal.get(), Thread.currentThread().getName());
                    sink.next(1);
                }
        ).publishOn(
                Schedulers.parallel()
        ).map(
                value -> {
                    // ParallelScheduler 로 실행
                    log.info("threadLocal: {}, tx: {}", threadLocal.get(), Thread.currentThread().getName());
                    return value;
                }
        ).publishOn(
                Schedulers.boundedElastic()
        ).map(
                value -> {
                    // BoundedElasticScheduler 로 실행
                    log.info("threadLocal: {}, tx: {}", threadLocal.get(), Thread.currentThread().getName());
                    return value;
                }
        ).subscribeOn(
                Schedulers.single()
        ).subscribe();


        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(1000); // 캐시 스케줄러는 데몬 스레드
    }


}
