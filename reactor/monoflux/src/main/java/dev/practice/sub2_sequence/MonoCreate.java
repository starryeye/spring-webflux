package dev.practice.sub2_sequence;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
public class MonoCreate {

    /**
     * [8]
     *
     * Mono 가 제공하는 create 메서드에 대해 알아본다.
     * MonoSink 에는 success 가 존재.. FluxSink 가 제공하는 메서드와 차이가 있는듯..
     *
     * create..
     * public static <T> Mono<T> create(Consumer<MonoSink<T>> callback)
     *
     * create 연산자는 MonoSink 라는 객체를 consume 하는 메서드이다.
     *
     * 참고.. MonoSink 가 제공하는 메서드.. (FluxSink 처럼 다양한 이벤트를 직접 다룰 수 있다.)
     * void success()
     *      바로 완료 처리
     * void success(@Nullable T value)
     *      T 타입의 객체를 item 으로 방출하고 완료 처리
     * void error(Throwable e)
     *      에러를 방출
     * default ContextView contextView()
     *      context 에 접근
     * MonoSink<T> onRequest(LongConsumer consumer);
     *      onRequest 이벤트를 발생시키고, 반환된 MonoSink 로 item 을 방출 시키던가 해볼 수 있을 듯..
     * MonoSink<T> onCancel(Disposable d);
     * MonoSink<T> onDispose(Disposable d);
     *      onRequest 와 비슷할 듯..
     *
     */

    public static void main(String[] args) {

        log.info("start main tx: {}", Thread.currentThread().getName());

        Mono<Flux<Integer>> monoWithFluxInside = Mono.create(monoSink -> {

            Flux<Integer> integerFlux = Flux.range(0, 10);

            monoSink.success(integerFlux);
        });

        monoWithFluxInside.flatMapMany(Function.identity()) // Mono<Flux<Integer>> 에서 Flux<Integer> 를 수행하면서 나오는 아이템과 이벤트를 방출한다.
                .doOnNext(item -> log.info("item: {}", item))
                .subscribe();


        log.info("end main tx: {}", Thread.currentThread().getName());
    }
}
