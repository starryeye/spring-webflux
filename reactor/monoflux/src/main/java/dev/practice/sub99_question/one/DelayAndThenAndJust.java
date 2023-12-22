package dev.practice.sub99_question.one;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
public class DelayAndThenAndJust {

    /**
     * just 내부의 메서드가 Delay 이전에 실행되어 버린다.... 이유는?
     *
     *
     * 참고>
     * Mono.fromCallable( Greeter::generate ) 로 하면 지연이 적용된다..
     */

    @SneakyThrows
    public static void main(String[] args) {
        log.info("start main, tx: {}", Thread.currentThread().getName());

        Mono.delay(Duration.ofMillis(5000L))
                .doOnSuccess(
                        value -> log.info("delay complete")
                )
                .then(
//                        Mono.fromCallable(Greeter::generate)
                        Mono.just(Greeter.generate())
                )
                .doOnNext(
                        value -> log.info("just next : {}", value)
                )
                .doOnSuccess(
                        value -> log.info("just complete")
                )
                .subscribe();


        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(6000L); // parallel 데몬 스레드 종료 방지
    }
}
