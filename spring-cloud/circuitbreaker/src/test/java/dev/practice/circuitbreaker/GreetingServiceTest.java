package dev.practice.circuitbreaker;

import dev.practice.circuitbreaker.config.AutoConfigureReactiveCircuitBreaker;
import dev.practice.circuitbreaker.config.TestCircuitBreakerConfig;
import lombok.extern.slf4j.Slf4j;
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

import java.time.Duration;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Slf4j
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
        Long delayMillis = 5000L;
        String circuitBreakerId = "default";

        // when
        // then
        StepVerifier.withVirtualTime(

                        // publisher 에 5초 딜레이를 걸어서 요청 후 응답이 5초 이후에 온 것 처럼 실행되도록 한다.
                        () -> greetingService.greeting("starryeye", delayMillis, circuitBreakerId)
                )
                .thenAwait(Duration.ofSeconds(1)) // virtual time 을 사용하여 1초 빨리감기를 수행하였다.
                .expectNext(FALLBACK_MESSAGE) // 서킷 브레이커는 기본 설정으로 1초 타임 아웃 정책을 가지므로, fallback 메시지가 응답된다.
                .verifyComplete();

        verify(greeter, never()).generate("starryeye"); // 5초 후 greeter::generate 메서드를 실행하기 때문에 실행 한 적이 없어야 정상이다.
    }

    @DisplayName("서킷브레이커에 의해 수행할 publisher(요청) 에서 exception 이 발생하면 fallback 메시지가 반환된다.")
    @Test
    void greeting_to_throw_exception() {

        // given
        String circuitBreakerId = "exception";

        // when
        Mono<String> result = greetingService.greetingWithException(circuitBreakerId);

        // then
        StepVerifier.create(result)
                .expectNext(FALLBACK_MESSAGE)
                .verifyComplete();

    }

}