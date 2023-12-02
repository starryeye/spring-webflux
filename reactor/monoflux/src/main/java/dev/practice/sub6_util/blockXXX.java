package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.stream.IntStream;

@Slf4j
public class blockXXX {

    /**
     * [11]
     * blockXXX 연산자에 대해 알아본다.
     *
     * public T block()
     * - Mono 의 경우 block 연산자가 제공 되며, Flux 의 경우 blockFirst, blockLast 등이 제공된다.
     * - 아래 예시에서는 publisher 작업 실행을 boundedElastic 가 하도록하였는데.. 그대로 두면 main 이 작업(publisher)을 수행한다.
     * - 즉, 호출(main) 스레드와, 작업(boundedElastic) 스레드 분리가 가능하다. (reactive stream 특징)
     * - 호출 스레드와 작업 스레드를 분리하면 block 이 되어있으므로 호출 스레드는 작업 스레드가 작업을 완료할때 까지 대기하게된다.
     * - return type 은 publisher 가 아닌.. value 그 자체이다.
     *
     * 참고
     * block 을 사용하면..
     * 1. 비동기 blocking 로직이 될 가능성이 높다.
     * 비동기 : reactive stream 연산자를 잘 사용해서 비동기로 만들수 있음
     * -> (아래 예제에서는 block 이후 로그를 남기는 로직이 있어서 동기가 되어버렸지만.. 이 경우도 그냥 파이프라인으로 처리할 수 있음)
     * blocking : block() 연산자 사용해서 blocking 로직이 됨
     * 따라서 사용을 지양해야 한다. (리액티브 프로그래밍의 효율성에 반하는 행동)
     *
     * 2. 데드락 발생 가능성이 있다.
     * 한정된 스레드로 동작한다고 해보자(2개)
     * 첫번째의 스레드(호출)로 blocking 해놓고 두번째 스레드가 작업을 하도록 해놓고..
     * 두번째 스레드(호출)도 blocking 해놓고 첫번째 스레드가 작업을 하도록 해놓은 상태라면..
     * 데드락이 걸릴듯 하다.
     *
     */

    public static void main(String[] args) {
        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.create(
                        sink -> {

                            IntStream.range(0, 3)
                                    .forEach(
                                            i -> {
                                                log.info("publisher next value: {}, tx: {}", i, Thread.currentThread().getName());
                                                sink.next(i);
                                            }
                                    );

                            log.info("publisher complete, tx: {}", Thread.currentThread().getName());
                            sink.complete();
                        }
                )
                .collectList()
                .doOnNext(value -> log.info("collectList value: {}, tx: {}", value, Thread.currentThread().getName()))
                .subscribeOn(
                        Schedulers.boundedElastic() // BoundedElasticScheduler 사용, 가변 스레드 풀
                ).block()
                .forEach(
                        value -> log.info("after block, value: {}, tx: {}", value, Thread.currentThread().getName())
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
