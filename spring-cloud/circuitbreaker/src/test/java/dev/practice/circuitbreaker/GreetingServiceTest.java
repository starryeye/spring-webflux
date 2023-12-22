package dev.practice.circuitbreaker;

import dev.practice.circuitbreaker.config.AutoConfigureReactiveCircuitBreaker;
import dev.practice.circuitbreaker.config.TestCircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Import(TestCircuitBreakerConfig.class) // test 를 위한 bean 들을 등록한다. (여러 서킷 브레이커 등록)
@AutoConfigureReactiveCircuitBreaker // 서킷 브레이커 테스트를 위한 어노테이션을 개발 (auto configuration)
@ExtendWith(SpringExtension.class) // 비어있는 Spring container 로 시작
@ContextConfiguration(
        classes = GreetingService.class // GreetingService 를 빈으로 등록
)
class GreetingServiceTest {

    @Autowired
    private GreetingService greetingService;

    private final String SUCCESS_MESSAGE = "hello, starryeye!";
    private final String FALLBACK_MESSAGE = "hello, world!";


    @Test
    void greetingNoDelay() {

        // given
        Long delayMillis = 0L;
        String circuitBreakerId = "default";

        // when
        Mono<String> result = greetingService.greeting("starryeye", delayMillis, circuitBreakerId);

        // then
        StepVerifier.create(result)
                .expectNext(SUCCESS_MESSAGE)
                .verifyComplete();

    }

}