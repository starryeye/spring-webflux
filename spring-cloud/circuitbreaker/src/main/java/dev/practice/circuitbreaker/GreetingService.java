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
@RequiredArgsConstructor
public class GreetingService {

    /**
     * todo, AOP 로 빼면 좋을 듯..
     *  -> @CircuitBreaker 어노테이션 알아보기
     */
    private final ReactiveCircuitBreakerFactory circuitBreakerFactory;
    private final Greeter greeter;

    private final String FALLBACK_MESSAGE = "hello, world!";


    // 요청 수행 메서드, test 를 위해서 circuitBreakerId 를 받도록 구현하였다.
    public Mono<String> greeting(String to, Long delayInMillis, String circuitBreakerId) {

        return doGreeting(to, delayInMillis) // 실제 요청 수행 publisher
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

    private Mono<String> doGreeting(String to, Long delayInMillis) {

        // 실제 요청 수행 publisher 를 반환한다.
        return Mono.delay(Duration.ofMillis(delayInMillis)) // delayInMillis 만큼 지연되고 greeter.generate 가 수행된다.
                .then(
                        Mono.fromCallable(
                                () -> greeter.generate(to)
                        )
                );
    }
}
