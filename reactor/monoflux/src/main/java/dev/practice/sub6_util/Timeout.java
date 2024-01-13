package dev.practice.sub6_util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Stream;

@Slf4j
public class Timeout {

    /**
     * [15]
     * timeout 연산자에 대해 알아본다.
     * - Flux::timeout() : item 간 publish 간격이 특정시간을 초과하면 exception 발생
     * - Mono::timeout() : item 이 특정 시간 동안 도착하지 않으면 exception 발생
     *
     * java.util.concurrent 의 TimeoutException 이 발생함.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.fromStream(Stream.of(1, 2, 3, 4, 5))
                .delayElements(Duration.ofSeconds(2L)) // 2초 마다 한개씩..
                .doOnNext(
                        value -> log.info("Flux Integer, doOnNext: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .doOnComplete(
                        () -> log.info("Flux Integer, doOnComplete tx: {}", Thread.currentThread().getName())
                )
                .timeout(Duration.ofSeconds(1L)) // item 사이 간격이 1초 이상 걸리면 exception 발생
                .onErrorComplete() // exception 발생 시, complete 처리
                .subscribe();

        Mono.delay(Duration.ofSeconds(3L)) // 3초 후 시작
                .then(Mono.just(1))
                .timeout(Duration.ofSeconds(2L)) // 2초 동안 전달 못받으면 timeout
                .onErrorComplete() // exception 발생 시, complete 처리
                .subscribe();

        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(10000);
    }
}
