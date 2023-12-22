package dev.practice.circuitbreaker;

import dev.practice.circuitbreaker.config.AutoConfigureReactiveCircuitBreaker;
import dev.practice.circuitbreaker.config.TestCircuitBreakerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;

@Import(TestCircuitBreakerConfig.class) // test 를 위한 bean 들을 등록한다. (여러 서킷 브레이커 등록)
@AutoConfigureReactiveCircuitBreaker // 서킷 브레이커 테스트를 위한 어노테이션을 개발 (auto configuration)
@ExtendWith(SpringExtension.class) // 비어있는 Spring container 로 시작
@ContextConfiguration(
        classes = GreetingService.class // GreetingService 를 빈으로 등록
)
class GreetingServiceTest {

    @Autowired
    private GreetingService greetingService;

    @SpyBean
    private Greeter greeter;

    private final String SUCCESS_MESSAGE = "hello, starryeye!";
    private final String FALLBACK_MESSAGE = "hello, world!";


    @DisplayName("모든 설정값이 기본 값이며, id 가 default 인 서킷 브레이커를 만들고 publisher(요청) 를 수행하면 성공한다.")
    @Test
    void greeting_no_delay() {

        // given
        Long delayMillis = 0L;
        String circuitBreakerId = "default";

        // when
        // 서킷 브레이커의 최초 상태는 close 상태이다. (정상 단계)
        Mono<String> result = greetingService.greeting("starryeye", delayMillis, circuitBreakerId);

        // then
        StepVerifier.create(result)
                .expectNext(SUCCESS_MESSAGE) // 기대한 응답을 검드
                .verifyComplete();

        verify(greeter).generate("starryeye"); // 요청이 수행되었다는 것을 검증

    }

    @DisplayName("서킷 브레이커는 기본 설정 값으로 요청 시 1초 타임아웃 정책을 가지고 있다.")
    @Test
    void greeting_delay_5000_and_wait_1000() {

        // given
        // when
        // then
    }

}