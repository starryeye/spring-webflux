package dev.practice.subscribe;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
public class WithSubscribe {

    /**
     * [2]
     *
     * public final Disposable subscribe()
     * - 별도로 전달되는 item 을 consume 하지 않고 최대한으로 item 을 요청한다.
     *
     * 참고로
     * subscribe 의 반환 타입인 Disposable 을 통하여 연결을 종료할 수 있다.
     */

    public static void main(String[] args) {
        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.fromIterable(List.of(1, 2, 3, 4, 5))
                .doOnNext( // 파이프라인에 영향을 주지 않고 지나가는 item 을 확인할 수 있다. (Consumer)
                        value -> {
                            log.info("value: {}, tx: {}", value, Thread.currentThread().getName());
                        }
                ).subscribe(); // subscribe 를 하였으므로 publisher 에 존재하는 item 들이 파이프라인을 따라서 흐른다.

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
