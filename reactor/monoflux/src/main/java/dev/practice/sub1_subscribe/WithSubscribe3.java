package dev.practice.sub1_subscribe;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
public class WithSubscribe3 {

    /**
     * [4]
     *
     * public final void subscribe(Subscriber<? super T> actual)
     *
     * Subscriber(Reactive Streams) 를 직접 구현하여 subscribe 메서드에 인자로 전달하면서
     * publisher 에 subscribe 하는 방법이다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.fromIterable(List.of(1, 2, 3, 4, 5))
                .subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        // publisher 가 전달한 subscription 을 받고, 즉시, Long.MAX_VALUE 개 만큼 item 을 요청한다.
                        // 즉시 흐름
                        s.request(Long.MAX_VALUE);
                        log.info("subscribe onSubscribe request: {}, tx: {}", Long.MAX_VALUE, Thread.currentThread().getName());
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info("subscribe onNext value: {}, tx: {}", integer, Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("subscribe onError error: {}, tx: {}", t, Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        log.info("subscribe onComplete tx: {}", Thread.currentThread().getName());
                    }
                });

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
