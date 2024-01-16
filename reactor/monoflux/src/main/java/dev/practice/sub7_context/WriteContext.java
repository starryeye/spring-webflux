package dev.practice.sub7_context;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

@Slf4j
public class WriteContext {

    /**
     * [3]
     *
     * 파이프라인 중간에 context 에 값을 쓰는 방법에 대해 알아본다.
     *
     * 특이점
     * - contextWrite 는 "위" 로 영향을 준다...
     * -> 그래서, publisher 에서 contextWrite 에서 수정한 값이 읽힌다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        // Context 생성
        Context initialContext = Context.of("name", "starryeye1");

        Flux.create(
                        fluxSink -> {
                            ContextView contextView = fluxSink.contextView(); // context 에 접근
                            String name = contextView.get("name");
                            // starryeye2 출력됨
                            log.info("publisher contextView name: {}, tx: {}", name, Thread.currentThread().getName());

                            fluxSink.next(name);
                            fluxSink.complete();
                        }
                )
                .contextWrite(
                        context -> context.put("name", "starryeye2") // context 수정한다.
                )
                .flatMap(
                        value -> Flux.create(
                                fluxSink -> {
                                    ContextView contextView = fluxSink.contextView(); // context 에 접근
                                    String name = contextView.get("name");
                                    // starryeye1 출력됨
                                    log.info("publisher2 contextView name: {}, tx: {}", name, Thread.currentThread().getName());

                                    fluxSink.next(name);
                                    fluxSink.complete();
                                }
                        )
                )
                .subscribe(
                        // value 는 downstream 으로 잘 전달될 것 이므로.. publisher2 에서 전달한 starryeye1 이다.
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        null,
                        null,
                        initialContext // 미리 생성했던 context 를 적용한다.
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
