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
     * - 여러개의 스레드가 존재할 때, 해당 스레드만 접근할 수 있는 특별한 저장소 (개념)
     * - 동일한 ThreadLocal 인스턴스를 여러 스레드에서 사용하더라도 각 스레드는 자신만의 값을 가질수있다.
     *      Thread -> ThreadLocalMap -> value 로 슬롯이 분리됨.
     *      값은 ThreadLocal 내부가 아닌 각 스레드의 ThreadLocalMap 에 저장된다.
     *
     * ThreadLocalMap
     * - Thread 마다 ThreadLocalMap 객체를 하나씩 보유한다.(내부 threadLocals 라는 필드명)
     * - ThreadLocal.set(v) 호출하면, 현재 실행 중인 스레드가 가진 ThreadLocalMap 에, 생성한 ThreadLocal 인스턴스를 키로 v 를 값으로 저장한다.
     * - Thread 가 죽으면 ThreadLocalMap 도 GC 대상
     *
     * publishOn, subscribeOn 으로 실행 스레드가 달라질 경우..
     * ThreadLocal 을 쓸 수 있을까?
     * -> 아래 실행결과를 참고 해봐도 되지만.. 기본적으로 전파되지 않는다.
     *      (Hooks.enableAutomaticContextPropagation 등을 쓰면 가능)
     *
     * 기존 Thread-per-Request 모델에서는 하나의 요청이 같은 워커 스레드에서 끝까지 처리되므로 ThreadLocal 값을 안전하게 참조할 수 있었다.
     * Event loop 모델에서는 중간에 스레드가 변경되고, 변경되면 ThreadLocal 이 기본적으로는 전파되지 않는다.
     * -> 요청 단위의 부가 정보를 안전하게 ‘흐름’으로 따라가게 하려면 Reactor Context(또는 Micrometer context-propagation 등)를 사용해야 한다.
     *      이는 ‘여러 스레드 간 동시 공유’라기보다는 체인 전반에 걸쳐 값을 전파(propagation) 하는 메커니즘에 가깝다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        // ThreadLocal 생성
        ThreadLocal<String> threadLocalInMain = new ThreadLocal<>();
        threadLocalInMain.set("main");

        log.info("threadLocal: {}, tx: {}", threadLocalInMain.get(), Thread.currentThread().getName());

        Flux.create(
                sink -> {
                    // SingleScheduler 로 실행
                    log.info("threadLocalInMain: {}, tx: {}", threadLocalInMain.get(), Thread.currentThread().getName());
                    // 람다에서 threadLocalInMain 지역 변수를 캡쳐하여, 동일한 인스턴스니까 뭔가 잘 접근될 것 같지만..
                    //      스레드가 달라졌기 때문에, 애초에 서로 다른 ThreadLocalMap 에서 찾으므로.. 값이 없다.. (null 리턴이다.)
                    //          -> 키(ThreadLocal 인스턴스)는 동일하지만 다른 공간(ThreadLocalMap) 이므로 없는 것.
                    sink.next(1);
                }
        ).publishOn(
                Schedulers.parallel()
        ).map(
                value -> {
                    // ParallelScheduler 로 실행
                    ThreadLocal<String> threadLocalInParallelScheduler = new ThreadLocal<>();
                    threadLocalInParallelScheduler.set("parallel");
                    log.info("threadLocalInParallelScheduler: {}, tx: {}", threadLocalInParallelScheduler.get(), Thread.currentThread().getName());
                    return value;
                }
        ).publishOn(
                Schedulers.boundedElastic()
        ).map(
                value -> {
                    // BoundedElasticScheduler 로 실행
                    log.info("threadLocalInMain: {}, tx: {}", threadLocalInMain.get(), Thread.currentThread().getName());
                    return value;
                }
        ).subscribeOn(
                Schedulers.single()
        ).subscribe();


        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(1000); // 캐시 스케줄러는 데몬 스레드
    }


}
