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
     * - publisher 를 생성하는 단계에서 sink 로 읽는 방법.. (중간에 flatMap, Publisher::create sink::contextView 로 해도 되긴함)
     * - 파이프라인 중간에서 읽기 위해서는.. flatMap, deferContextual 을 합성해서 읽는 방법..
     *
     * 참고로..
     * 파이프라인 중간에서 contextWrite 로 context 를 접근해서 읽어도 되긴하지만.. Function 으로 context 만을 받고 반환하므로..
     * downstream 으로 전달되는 값을 조작할 순 없다.
     *
     * 주의사항
     * contextWrite 는 onNext event 에 따라 publisher 에서 subscribe 까지 downstream 순으로 실행되는 연산자가 아니다.
     * element 가 흐르기 전에 초기에 수행되며 contextWrite 연산자 끼리의 순서는 upstream 으로 실행된다.
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
                                    // starryeye 4 출력
                                    log.info("flatMap deferContextual 1, name: {}, tx: {}", name, Thread.currentThread().getName());
                                    return Mono.just(v);
                                }
                        )
                )
                .contextWrite(
                        context -> {
                            log.info("read by contextWrite 1, name: {}, tx: {}", context.get("name"), Thread.currentThread().getName());
                            return context.put("name", "starryeye 4");
                        }
                )
                .flatMap(
                        v -> Mono.deferContextual(
                                contextView -> {
                                    String name = contextView.get("name");
                                    // starryeye 3 출력
                                    log.info("flatMap deferContextual 2, name: {}, tx: {}", name, Thread.currentThread().getName());
                                    return Mono.just(v);
                                }
                        )
                )
                .contextWrite(
                        context -> {
                            log.info("read by contextWrite 2, name: {}, tx: {}", context.get("name"), Thread.currentThread().getName());
                            return context.put("name", "starryeye 3");
                        }
                )
                .flatMap(
                        v -> Mono.deferContextual(
                                contextView -> {
                                    // starryeye 2 출력
                                    String name = contextView.get("name");
                                    log.info("flatMap deferContextual 3, name: {}, tx: {}", name, Thread.currentThread().getName());
                                    return Mono.just(v);
                                }
                        )
                )
                .contextWrite(
                        context -> context.put("name", "starryeye 2")
                )
                .contextWrite( // contextWrite 를 통해 context 를 읽어도 되긴한다. (그래도 역순을 잊으면 안된다.)
                        // 주의사항은 downstream 순서대로 실행되는 연산자가 아니고 upstream 으로 수행되며, onNext 이벤트에 의해 실행되는 연산자가 아니다.
                        // 로그를 보면 flatMap deferContextual 3 다음에 read by contextWrite 3 이 찍히지 않음을 알 수 있다.
                        context -> {
                            // starryeye 1 찍힘
                            log.info("read by contextWrite 3, name: {}, tx: {}", context.get("name"), Thread.currentThread().getName());
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
