package dev.practice.sub4_error;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class OnErrorReturn2 {

    /**
     * [4]
     *
     * onErrorReturn 을 사용할 때 주의사항..
     *
     * 고정된 값을 넘기기 위해 함수를 사용하면.. 문제가 발생할 수 있다.
     * -> 에러가 발생하지 않더라도.. onErrorReturn 의 함수를 "무조건 실행"한 후 값을 사용한다.
     * -> 예상 원인) 파이프라인을 구축하면서 실행해버리나봄..
     */

    public static void main(String[] args) {


        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.just(1) // publisher 는 1 을 전달한다.
                .onErrorReturn( // 에러 처리를 위해 onErrorReturn 사용
                        shouldDoOnError() // 해당 함수는 에러가 발생되지 않아도 실행된다.
                )
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName())
                );

        /**
         * 분명히, shouldDoOnError 함수는
         * onErrorReturn 연산자가 실행될 때 실행될 것이라고 생각했고..
         * 현재 publisher 에서는 에러를 발생시키지 않는 상황이다..
         * 따라서 shouldDoOnError 함수는 실행되지 않을 것이라 예측 했지만..
         *
         * 로그 결과를 보면 shouldDoOnError 함수가 실행되는 모습을 확인 할 수 있다. (주의 해야함)
         *
         */

        log.info("end main tx: {}", Thread.currentThread().getName());
    }

    private static int shouldDoOnError() {
        log.info("shouldDoOnError, tx: {}", Thread.currentThread().getName());
        return 0; // 에러가 발생하면 고정 값으로 0 을 사용
    }
}
