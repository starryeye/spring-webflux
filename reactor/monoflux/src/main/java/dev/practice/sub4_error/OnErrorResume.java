package dev.practice.sub4_error;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Slf4j
public class OnErrorResume {

    /**
     * [5]
     *
     *
     * onErrorResume 연산자
     * - 기존 publisher 나 연산자 실행 과정에서 발생된 onError 이벤트를 처리하기 위함이다.
     * - onError 이벤트가 발생되면 새로운 publisher 를 반환한다.
     * - 반환된 publisher 의 onNext, onError, onComplete 이벤트가 downstream 으로 전달된다.
     *
     * 참고
     * onErrorReturn 과 다르게 onErrorResume 은...
     * 에러가 발생하지 않으면 apply 를 실행하지 않는다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.error(new RuntimeException("error")) // 기존 publisher 에서 onError 이벤트 발생
                .onErrorResume(new Function<Throwable, Publisher<Integer>>() { // onErrorResume 적용
                    @Override
                    public Publisher<Integer> apply(Throwable throwable) { // 기존 publisher 에서 "onError 이벤트가 발생되면" 실행된다.

                        log.info("onErrorResume publisher, tx: {}", Thread.currentThread().getName());
                        return Flux.just(0, -1, -2); // 에러를 새로운 publisher 로 대체
                    }
                })
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("subscribe complete, tx: {}", Thread.currentThread().getName())
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
