package dev.practice.sub1_subscribe;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static reactor.core.publisher.Signal.subscribe;

@Slf4j
public class WithSubscribe4 {

    /**
     * [5]
     *
     * public abstract void subscribe(CoreSubscriber<? super T> actual)
     *
     * subscribe 의 인자로 Reactor 의 CoreSubscriber 를 전달하여..
     * publisher 에 subscribe 할 수 있다.
     *
     * CoreSubscriber 는 Reactive Streams 의 Subcriber 를 상속한다.
     * BaseSubscriber 는 CoreSubscriber 를 구현한다.
     *
     * BaseSubscriber..
     * Subscriber 의 추상메서드의 이름을 좀 변경하였고 추가 기능이 존재한다.
     * - subscriber 에서 subscription 의 기능을 사용할 수 있다.
     * - 더 다양한 사용법과 기능은 PDF 자료 참조
     *
     * CoreSubscriber, BaseSubscriber 모두 Reactor (reactorproject) 에 포함된다.
     *
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        BaseSubscriber<Integer> subscriber = new BaseSubscriber<>() {
            @Override
            protected void hookOnNext(Integer value) {
                log.info("BaseSubscriber hookOnNext value: {}, tx: {}", value, Thread.currentThread().getName());
            }

            @Override
            protected void hookOnComplete() {
                log.info("BaseSubscriber hookOnComplete, tx: {}", Thread.currentThread().getName());
            }

            @Override
            protected void hookOnCancel() {
                log.info("BaseSubscriber hookOnCancel, tx: {}", Thread.currentThread().getName());
            }
        };

        Flux.fromIterable(List.of(1, 2, 3, 4, 5))
                .delayElements(Duration.ofSeconds(2)) // 2 초 간격으로 item 흐름
                .subscribe(subscriber);

        // subscriber 에서 subscription 의 기능을 사용할 수 있다.
//        subscriber.request(5); // 1 개의 item 을 요청 (기본 값은 unbounded request)

        Thread.sleep(5000); // 5 초 지연

        // subscriber 에서 subscription 의 기능을 사용할 수 있다.
        subscriber.cancel(); // subscribe 취소

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
