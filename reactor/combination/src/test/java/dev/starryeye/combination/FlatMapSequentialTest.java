package dev.starryeye.combination;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class FlatMapSequentialTest {

    @Test
    public void custom_test() {
        /**
         * practical-reactor 에서
         * c6_CombiningPublishers, task_executor_again
         * c9_ExecutionControl, sequential_free_runners
         * 와 연관 되어 생긴 의문을 정확하게 해소하는 목적..
         *
         * 의문점..
         * flatMapSequential 에 여러 개의 publisher 가 item 으로 들어오게 되었고..
         * 각 publisher 를 병렬적으로 구독하는 상황이다.
         * flatMapSequential 에서 보장해주는 순서는 각 publisher 가 구독된 순서인가..
         * 아니면.. flatMapSequential 에 publisher 가 들어온 순서일까..
         *
         * -> 일단, item(publisher) 이 들어온 순서인듯 하다.
         */

        // 100개의 Flux 생성
        Flux<Flux<Integer>> sources = Flux.fromIterable(
                IntStream.range(0, 100)
                        .mapToObj(
                                i -> Flux.just(i)
                                        .doOnSubscribe(subscription -> log.info("i am subscribed.."))
                                        .delayElements(Duration.ofMillis(100))
                                        .doOnNext(item -> log.info("before item : {}", item))
                                        .subscribeOn(Schedulers.parallel())
                        )
                        .collect(Collectors.toList())
        );

        // flatMapSequential을 사용하여 순서 보장
        Flux<Integer> result = sources.flatMapSequential(flux -> flux)  //Function.identity()
                .doOnNext(i -> log.info("after item : {}", i))
                ;

        // StepVerifier로 결과 확인
        StepVerifier.create(result)
                .expectNextSequence(IntStream.range(0, 100).boxed().collect(Collectors.toList()))
                .verifyComplete();
    }
}
