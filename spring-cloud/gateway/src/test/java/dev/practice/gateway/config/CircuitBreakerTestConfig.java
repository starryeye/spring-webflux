package dev.practice.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@Slf4j
@TestConfiguration
public class CircuitBreakerTestConfig {

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> myCircuitBreakerFactory() {

        /**
         * MyCircuitBreaker (cb id) 에 대한 factory 설정.. 기본 설정은 application-circuitbreaker.yml 로 했으므로 여기서는 logger 만 등록해준다.
         */

        String[] targetCircuitBreakerId = new String[]{"MyCircuitBreaker"};

        return reactiveResilience4JCircuitBreakerFactory -> {

            // logger 등록
            reactiveResilience4JCircuitBreakerFactory.addCircuitBreakerCustomizer(getEventLogger(), targetCircuitBreakerId);
        };
    }

    private Customizer<CircuitBreaker> getEventLogger() {

        /**
         * 서킷 브레이커의 이름을 키로 하여 서킷브레이커를 커스텀 할 수 있는 곳이다.
         * 여기서는 서킷 브레이커의 EventPublisher 를 받아서 서킷 브레이커의 이벤트가 발생하면 로그를 남기도록 하였다.
         */

        return Customizer.once(
                circuitBreaker -> {

                    String circuitBreakerName = circuitBreaker.getName();
                    circuitBreaker.getEventPublisher()
                            .onSuccess(
                                    event -> log.info("circuit breaker : {}, success", circuitBreakerName)
                            )
                            .onError(
                                    event -> log.info("circuit breaker : {}, error : {}", circuitBreakerName, event.getThrowable().toString())
                            )
                            .onStateTransition(
                                    event -> log.info(
                                            "circuit breaker : {}, state changed from : {} to : {}",
                                            circuitBreakerName,
                                            event.getStateTransition().getFromState(),
                                            event.getStateTransition().getToState()
                                    )
                            )
                            .onSlowCallRateExceeded(
                                    event -> log.info("circuit breaker : {}, slow call rate exceeded : {}", circuitBreakerName, event.getSlowCallRate())
                            )
                            .onFailureRateExceeded(
                                    event -> log.info("circuit breaker : {}, failure rate exceeded: {}", circuitBreakerName, event.getFailureRate())
                            );
                },
                CircuitBreaker::getName
        );
    }
}
