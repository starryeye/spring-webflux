package dev.practice.stream.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Supplier;

public class SequenceFluxStringTest {

    private StreamFunctionConfig streamFunctionConfig = new StreamFunctionConfig();

    @DisplayName("StreamFunctionConfig 에서 등록할 sequenceFluxString 을 검증한다.")
    @Test
    void test() {

        /**
         * sequenceFluxString 은 one, two, three 를 순서대로 흘려보내는 Flux<String> 을 제공하는 Supplier 이다.
         */

        // given
        // when
        Supplier<Flux<String>> supplier = streamFunctionConfig.sequenceFluxString();
        Flux<String> flux = supplier.get();

        // then
        StepVerifier.create(flux)
                .expectNext("one")
                .expectNext("two")
                .expectNext("three")
                .verifyComplete();
    }
}
