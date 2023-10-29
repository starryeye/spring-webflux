package dev.practice.sub7_context;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Slf4j
public class DeferContextual2 {

    /**
     * 좀더 복잡하게 해서 이해를 완벽하게 해보자.
     *
     * contextWrite 는 파이프라인 역순으로 적용된다.....
     *
     * context 를 읽기 위해서는 2가지 방법이 있다.
     * - publisher 를 생성하는 단계에서 sink 로 읽는 방법..
     * - 파이프라인 중간에서 읽기 위해서는.. flatMap, deferContextual 을 합성해서 읽는 방법..
     *
     * 참고로..
     * 파이프라인 중간에서 contextWrite 로 context 를 접근해서 읽으면 안된다.
     * -> contextWrite 는 publisher 에서 subscribe 까지 downstream 순으로 실행되는 연산자가 아니기 때문이다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Context initContext = Context.empty().put("name", "starryeye 1");

        // 아래의 flatMap, deferContextual 는 단순히 파이프라인 중간에 context 를 읽기 위함이다..
        Flux.just(1)
                .flatMap(
                        v -> Mono.deferContextual(
                                contextView -> {
                                    String name = contextView.get("name");
                                    log.info("flatMap deferContextual 1, name: {}, tx: {}", name, Thread.currentThread().getName());
                                    return Mono.just(v);
                                }
                        )
                )
                .contextWrite(
                        context -> context.put("name", "starryeye 4")
                )
                .flatMap(
                        v -> Mono.deferContextual(
                                contextView -> {
                                    String name = contextView.get("name");
                                    log.info("flatMap deferContextual 2, name: {}, tx: {}", name, Thread.currentThread().getName());
                                    return Mono.just(v);
                                }
                        )
                )
                .contextWrite(
                        context -> context.put("name", "starryeye 3")
                )
                .flatMap(
                        v -> Mono.deferContextual(
                                contextView -> {
                                    String name = contextView.get("name");
                                    log.info("flatMap deferContextual 3, name: {}, tx: {}", name, Thread.currentThread().getName());
                                    return Mono.just(v);
                                }
                        )
                )
                .contextWrite(
                        context -> context.put("name", "starryeye 2")
                )
                .contextWrite( // 위에 처럼 복잡하게 읽지 말고 그냥 이렇게 읽으면 안되나.. 라고 생각하는 것은 틀린 생각이다..
                        // 이 연산자는 downstream 순서대로 실행되는 연산자가 아니기 때문이다.
                        // 로그를 보면 flatMap deferContextual 3 다음에 read by contextWrite 가 찍히는게 아님!
                        context -> {
                            log.info("read by contextWrite, name: {}, tx: {}", context.get("name"), Thread.currentThread().getName());
                            return context;
                        }
                )
                .subscribe(
                        null,
                        null,
                        null,
                        initContext
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
