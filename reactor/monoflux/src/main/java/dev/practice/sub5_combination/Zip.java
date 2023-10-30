package dev.practice.sub5_combination;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
public class Zip {

    /**
     * [5]
     *
     * zip 연산자를 알아보겠다.
     *
     * - 이전과 마찬가지로 두개의 소스(publisher)를 병합하는 기능을 가진다.
     * - 여러개의 소스가 zip 으로 결합되어있을때, 모든 소스들에서 하나씩 element 가 전달되면..
     * zip 에서 하나의 tuple 로 묶어서 downstream 으로 전달한다.
     * - 여러개의 소스에서 element 들을 받는데 한군데에서 안주면 downstream 으로 tuple 이 전달되지 않는다.
     * - 여러개의 소스들의 element 의 개수가 동일하지 않아도 된다. 알잘딱깔센으로 묶을 수 있는 tuple 까지만 전달하고 완료 처리된다.
     */


    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux<Integer> flux1 = Flux.range(1, 20)
                .doOnNext(
                        value -> log.info("flux 1, before zip, value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .doOnComplete(
                        () -> log.info("flux 1, complete, tx: {}", Thread.currentThread().getName())
                )
                .delayElements(
                        Duration.ofMillis(300)
                );
        Flux<Integer> flux2 = Flux.range(101, 22)
                .doOnNext(
                        value -> log.info("flux 2, before zip, value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .doOnComplete(
                        () -> log.info("flux 1, complete, tx: {}", Thread.currentThread().getName())
                )
                .delayElements(
                        Duration.ofMillis(100)
                );

        Flux.zip(flux1, flux2)
                .publishOn(
                        Schedulers.single()
                )
                .map(
                        tuple -> {
                            log.info("zip tuple t1: {}, t2: {}, tx: {}", tuple.getT1(), tuple.getT2(), Thread.currentThread().getName());
                            return "%s + %s".formatted(tuple.getT1(), tuple.getT2());
                        }
                )
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        null,
                        () -> log.info("subscribe complete, tx: {}", Thread.currentThread().getName())
                );


        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(10000);
    }
}
