package dev.practice.subscribe;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
public class WithoutSubscribe {

    /**
     * [1]
     * Publisher 에 subscribe 를 하지 않는다면.. 아무일도 생기지 않는다.
     *
     * - 컵 속에 빨대를 그냥 꼽아 놓은 것과 비슷하다.
     * - Stream 에 종료 오퍼레이션을 적용하지 않은 것과 비슷하다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.fromIterable(List.of(1, 2, 3, 4, 5))
                        .doOnNext(
                                value -> {
                                    log.info("value: {}, tx: {}", value, Thread.currentThread().getName());
                                }
                        ); // publisher 를 subscribe 하지 않았으므로 아무일도 일어나지 않는다.

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
