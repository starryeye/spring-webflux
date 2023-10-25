package dev.practice.sub4_error;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Slf4j
public class OnErrorMap {

    /**
     * [7]
     *
     * onErrorMap 연산자
     *
     * - onError 이벤트의 원인을 변경한다.
     *
     * 에러에서 에러로 변환 하므로 에러 핸들링은 따로 해줘야함
     */


    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.error(new IOException("fail to read file")) // IOException 으로 onError 이벤트 전달
                .onErrorMap(
                        e -> new CustomBusinessException("custom") // CustomBusinessException 으로 onError 이벤트 변경하여 전달
                )
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.info("subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("subscribe complete, tx: {}", Thread.currentThread().getName())
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }


    private static class CustomBusinessException extends RuntimeException {
        public CustomBusinessException(String message) {
            super(message);
        }
    }
}
