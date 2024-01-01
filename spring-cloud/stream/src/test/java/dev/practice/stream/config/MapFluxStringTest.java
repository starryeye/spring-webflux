package dev.practice.stream.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;
import java.util.stream.Stream;

public class MapFluxStringTest {

    private StreamFunctionConfig streamFunctionConfig = new StreamFunctionConfig();

    @DisplayName("StreamFunctionConfig 에서 등록할 mapFluxString 을 검증한다.")
    @Test
    void test() {

        /**
         * 주어진 Flux<String> 의 element 에 hello, XXX! 를 붙이는 Function 이다.
         */

        // given
        Stream<String> givenStream = Stream.of("A", "B", "C");
        Flux<String> givenFlux = Flux.fromStream(givenStream);

        // when
        Function<Flux<String>, Flux<String>> function = streamFunctionConfig.mapFluxString();
        Flux<String> result = function.apply(givenFlux);

        // then
        StepVerifier.create(result)
                .expectNext("hello, A!")
                .expectNext("hello, B!")
                .expectNext("hello, C!")
                .verifyComplete();

    }
}
