package dev.practice.sub4_error;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class WithoutErrorHandle {

    /**
     * [1]
     *
     * Reactive streams 에서 onError 이벤트가 발생하면 더 이상..
     * onNext, onComplete 이벤트를 전달하지 않고 종료한다.
     *
     * Reactor 에서는.. onError 이벤트가 발생하면 ..
     * onError 이벤트를 파이프라인으로 전파하고..
     * Reactive streams 와 동일하게 종료된다.
     *
     * onError 이벤트는 처리가 필요하다.
     * 에러 처리를 위한 4가지 방법
     * - 에러가 발생되면 고정된 값을 전달
     * - 에러가 발생되면 새로운 publisher 를 전달하고 해당 publisher 의 이벤트로 재개
     * - 에러가 발생되면 onComplete 이벤트로 변경
     * - 에러가 발생되면 다른 에러로 변경
     *
     *
     * 에러 처리가 없는 상황을 가정해보겠다..
     * Source, 연산자 작업도중 에러 발생하는 경우 ...
     * 일단 중간 연산자에서 에러 처리가 없어서..
     * onError 이벤트가 downstream 으로 subscriber 까지 전달되고..
     * subscriber 에서도 에러 처리가 없으므로..
     * 마지막 안전장치인 onErrorDropped 훅을 호출한다.
     * - onErrorDropped 는 단순히 에러를 출력한다 (사용자가 오버라이딩 가능)
     */

    public static void main(String[] args) {

        log.info("start main");

        Flux.create(
                        sink -> sink.error(new RuntimeException("sink error")) // FluxSink 로 명시적 error 이벤트 발생
//                        sink -> { throw new RuntimeException("throw exception"); } // throw 로 발생 시켜도 동일한듯..
                )
                .subscribe();
        // 에러가 발생하면 downstream 으로 onError 이벤트가 전파된다.
        // 현재 중간 연산자나 subscriber 에서 에러를 처리하고 있지 않으므로 ..
        // 최종 안전장치인 onErrorDropped 훅이 호출되어.. 에러를 출력한다.(기본 구현)
        // onError 이후 더 이상의 onNext, onComplete 는 발생되지 않음


        log.info("end main");
    }
}
