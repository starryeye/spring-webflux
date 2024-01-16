package dev.practice.sub7_context;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class DeferContextual {

    /**
     * [4]
     *
     * deferContextual 연산자에 대해 알아본다.
     *
     * - defer 연산자와 비슷하지만, supplier 가 아닌.. contextView 를 인자로 받는 Functional 을 받는다.
     * - 반환한 publisher 의 이벤트를 downstream 으로 전달하는 측면에선 defer 와 동일하다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        // DeferAndFlatMap.java 에 의해 아래는 결국 Mono.just(1) 이다.
        // contextView 를 파이프라인 중간에서 읽기 위해 아래와 같은 패턴을 사용한 것이다.. (initialAndReadContext 에서는 중간에 읽기 위해 create, sink 를 이용중)
        Mono.just(1) // publisher 끝
                .flatMap( // 파이프라인 중간 연산자
                        value -> Mono.deferContextual( // deferContextual
                                contextView -> {
                                    String name = contextView.get("name");
                                    log.info("name: " + name);
                                    return Mono.just(value);
                                }
                        )
                )
                .contextWrite(
                        context -> context.put("name", "starryeye")
                ).subscribe();

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }

}
