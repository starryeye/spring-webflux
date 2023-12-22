package dev.practice.circuitbreaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
public class GreetingService {

    private final ReactiveCircuitBreakerFactory circuitBreakerFactory; // todo, AOP 로 빼면 좋을 듯..

    private final String MESSAGE = "hello, %s!";
    private final String FALLBACK_MESSAGE = "hello, world!";

    private Long delayInMillis;


    public GreetingService(ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.delayInMillis = 0L;
    }

    // test 를 위해서 delayInMillis 를 set 할 수 있도록하였다. fluent 하게 코드를 작성하도록 chaining 지원
    public GreetingService setDelayInMillis(Long delayInMillis) {
        this.delayInMillis = delayInMillis;
        return this;
    }

    // 요청 수행 메서드, test 를 위해서 circuitBreakerId 를 받도록 구현하였다.
    public Mono<String> greeting(String to, String circuitBreakerId) {

        return doGreeting(to) // 실제 요청 수행 publisher
                .transform(
                        publisher -> {
                            // 서킷 브레이커 생성
                            ReactiveCircuitBreaker reactiveCircuitBreaker = circuitBreakerFactory.create(circuitBreakerId);
                            /**
                             * 생성한 서킷 브레이커로 요청을 수행
                             * - close 상태면, publisher 를 수행한다
                             * - open 상태면, publisher 를 수행하지 않고 fallback message 를 반환한다.
                             * - half open 상태면, publisher 를 수행하면서 검증 단계를 거친다.
                             */
                            return reactiveCircuitBreaker.run(publisher, throwable -> Mono.just(FALLBACK_MESSAGE));
                        }
                );
    }

    private Mono<String> doGreeting(String to) {

        // 실제 요청 수행 publisher 를 반환한다.
        return Mono.delay(Duration.ofMillis(this.delayInMillis))
                .then(
                        Mono.just(MESSAGE.formatted(to))
                );
    }
}
