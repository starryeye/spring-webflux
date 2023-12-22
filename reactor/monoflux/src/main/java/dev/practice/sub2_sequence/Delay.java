package dev.practice.sub2_sequence;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class Delay {

    /**
     * [11]
     * Mono.delay 에 대해 알아본다.
     *
     * - delay 로 전달된 시간 만큼 지연된 이후 element 가 전달된다.
     * - 기본적으로 publisher 실행은 parallel scheduler 가 수행한다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}, now : {}", Thread.currentThread().getName(), LocalDateTime.now());

        Mono.delay(Duration.ofMillis(5000L)) // 5초 간 딜레이
                .doOnNext(
                        value -> log.info("first doOnNext, value : {}, tx: {}, now : {}", value, Thread.currentThread().getName(), LocalDateTime.now())
                )
                .then(
                        Mono.delay(Duration.ofMillis(5000L)) // 5초 간 딜레이
                )
                .doOnNext(
                        value -> log.info("second doOnNext, value : {}, tx: {}, now : {}", value, Thread.currentThread().getName(), LocalDateTime.now())
                )
                .then(
                        Mono.just("item")
                )
                .doOnNext(
                        value -> log.info("third doOnNext, value : {}, tx: {}, now : {}", value, Thread.currentThread().getName(), LocalDateTime.now())
                )
                .subscribe();

        log.info("end main, tx: {}, now : {}", Thread.currentThread().getName(), LocalDateTime.now());

        Thread.sleep(12000L); // parallel scheduler 는 데몬 스레드
    }
}
