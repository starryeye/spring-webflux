package dev.practice.sub7_context;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

@Slf4j
public class InitialAndReadContext {

    /**
     * [2]
     *
     * context 를 생성하고 적용하고 읽는 방법에 대해 알아본다.
     *
     * 특이점
     * - context 는 immutable 객체이다.
     *
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        // Context 생성
        Context initialContext = Context.of("name", "starryeye");

        Flux.create(
                    fluxSink -> {
                        ContextView contextView = fluxSink.contextView(); // context 에 접근
                        String name = contextView.get("name");
                        log.info("publisher contextView name: {}, tx: {}", name, Thread.currentThread().getName());

                        fluxSink.next(name);
                    }
                )
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        null,
                        null,
                        initialContext // 미리 생성했던 context 를 적용한다.
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
