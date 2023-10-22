package dev.practice.sub1_subscribe;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import java.util.List;

@Slf4j
public class WithSubscribe2 {

    /**
     * [3]
     *
     * 	public final Disposable subscribe(
     *            @Nullable Consumer<? super T> consumer,
     *            @Nullable Consumer<? super Throwable> errorConsumer,
     *            @Nullable Runnable completeConsumer,
     *            @Nullable Context initialContext
     *            )
     *
     *  4 개의 함수형 인터페이스 구현체를 subscribe 의 인자로 전달하면서
     *  publisher 에 subscribe 하는 방법이다.
     *
     *  4 개 중 3 개의 인자는 Reactive Streams 의 Subscriber 인터페이스의 추상 메서드에 해당한다고 생각하면 편하다.
     *  대신, onSubscription 추상 메서드에 해당되는게 없으므로 subscription 개념을 사용하지 못한다. (backpressure X)
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.fromIterable(List.of(1, 2, 3, 4, 5))
                .doOnNext( // 파이프라인에 영향을 주지 않고 지나가는 item 을 확인할 수 있다. (Consumer)
                        value -> {
                            log.info("value: {}, tx: {}", value, Thread.currentThread().getName());
                        }
                ).subscribe(
                        value -> {
                            // value 는 전달되는 item 이다.
                            // 파이프라인으로 전달되는 item 이 하나씩 전달된다. (Consumer, 여기가 파이프라인의 끝이다.)
                            log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName());
                        }, error -> {
                            // error 는 전달되는 Throwable 이다.
                            // 파이프라인에서 에러 이벤트가 발생되면 여기가 호출된다. (Consumer, 여기가 파이프라인의 끝이다.)
                            log.info("subscribe error: {}, tx: {}", error, Thread.currentThread().getName());
                        }, () -> {
                            // complete 이벤트가 발생되면 여기가 호출된다. (Runnable, 여기가 파이프라인의 끝이다. 파이프라인의 흐름 자체도 끝을 의미한다.)
                            log.info("subscribe complete, tx: {}", Thread.currentThread().getName());
                        }, Context.empty() // upstream 에 전달할 context (Context)
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
