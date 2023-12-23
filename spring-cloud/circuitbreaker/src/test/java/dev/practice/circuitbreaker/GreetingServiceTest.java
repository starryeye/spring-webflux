package dev.practice.circuitbreaker;

import dev.practice.circuitbreaker.config.AutoConfigureReactiveCircuitBreaker;
import dev.practice.circuitbreaker.config.TestCircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
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
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

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

    @DisplayName("서킷 브레이커 상태를 close 에서 open 으로 전환 시켜본다.")
    @Test
    void make_circuit_breaker_open() {

        // given
        String circuitBreakerId = "mini"; // TestCircuitBreakerConfig 에 mini 이름을 가진 서킷 브레이커에 대한 설정이 있다.

        Mono<String> requestWithNoDelay = greetingService.greeting("starryeye", 0L, circuitBreakerId);
        // 처음 4회는 정상 수행 (sliding window 가 4 라서 4회 동안 close 상태로 진행, delay 를 0 으로 잡아서 time out 걸리지 않아 정상 처리)
        // sliding window 가 다 채워지기 전 까지는 무조건 close 상태이다. -> failureRate 검사는 모두 채워져야 계산한다.
        IntStream.range(0, 4)
                .forEach(
                        i -> StepVerifier.create(requestWithNoDelay)
                                .expectNext(SUCCESS_MESSAGE) // no delay 로 진행시켜서 Greeter::generate 가 수행됨
                                .verifyComplete()
                );

        // when
        Mono<String> requestWithDelay = greetingService.greeting("starryeye", 5000L, circuitBreakerId);
        // sliding window 가 모두 채워졌으므로 2회를 실패 시키면 failureRate 50% 만족하여 close 에서 open 으로 전환될 것이다.
        IntStream.range(0, 2)
                .forEach(
                        // 딜레이 요청으로 타임아웃 (기본 값 1초) 걸려서 실패 처리됨
                        i -> StepVerifier.withVirtualTime(() -> requestWithDelay)
                                .thenAwait(Duration.ofSeconds(2)) // 2초 빨리 감기
                                .expectNext(FALLBACK_MESSAGE)
                                .verifyComplete()
                );

        // then
        // 해당 서킷브레이커는 open 상태이다.
        assertEquals(CircuitBreaker.State.OPEN, circuitBreakerRegistry.circuitBreaker(circuitBreakerId).getState());

        IntStream.range(0, 100)
                .forEach(
                        i -> StepVerifier.create(requestWithNoDelay)
                                .expectNext(FALLBACK_MESSAGE) // no delay 로 수행하였지만, open 상태라서 Greeter::generate 가 수행되지 않는다.
                                .verifyComplete()
                );

        verify(greeter, times(4)).generate("starryeye");

    }

    @DisplayName("open 상태의 서킷브레이커를 수동으로 half-open 상태로 만들어본다.")
    @Test
    void make_circuit_breaker_half_open_manually() {
        // 서킷 브레이커 상태를 수동으로 관리하는 방법은 지양해야한다.

        // given
        String circuitBreakerId = "mini";

        Mono<String> requestWithDelay = greetingService.greeting("starryeye", 5000L, circuitBreakerId);

        // 4 회 실패 시켜서 open 상태가 된다.
        IntStream.range(0, 4)
                .forEach(
                        i -> StepVerifier.withVirtualTime(() -> requestWithDelay)
                                .thenAwait(Duration.ofSeconds(2))
                                .expectNext(FALLBACK_MESSAGE)
                                .verifyComplete()
                );

        Mono<String> requestWithNoDelay = greetingService.greeting("starryeye", 0L, circuitBreakerId);
        StepVerifier.create(requestWithNoDelay)
                .expectNext(FALLBACK_MESSAGE) // no delay 로 진행했지만, open 상태라 Fallback message 가 반환된다. (Greeter::genreate 가 수행되지 않음)
                .verifyComplete();


        // when
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerId);
        log.info("change state to half-open manually");
        circuitBreaker.transitionToHalfOpenState(); // 수동으로 half-open 상태로 변경

        // then
        CircuitBreaker.State currentState = circuitBreakerRegistry.circuitBreaker(circuitBreakerId).getState();
        assertEquals(CircuitBreaker.State.HALF_OPEN, currentState);

        // half-open 상태에서는 검증을 위해 publisher(요청) 를 수행시킨다. 해당 요청은 no delay 로 진행시켜서 Greeter::generate 가 수행됨을 알수 있다.
        StepVerifier.create(requestWithNoDelay)
                .expectNext(SUCCESS_MESSAGE)
                .verifyComplete();

        verify(greeter, times(1)).generate("starryeye");
    }

}