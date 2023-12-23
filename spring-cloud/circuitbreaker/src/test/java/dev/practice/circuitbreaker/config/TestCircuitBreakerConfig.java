package dev.practice.circuitbreaker.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.List;

@Slf4j
@TestConfiguration
public class TestCircuitBreakerConfig {

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


    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultFactory() {

        // (서킷 브레이커가 생성되진 않음, factory.create 를 통해 생성해서 사용해야함, 예약 설정일 뿐이다.)

        return reactiveResilience4JCircuitBreakerFactory -> {
            reactiveResilience4JCircuitBreakerFactory.configureDefault( // 별도의 설정을 가지지 않는 서킷 브레이커들은 해당 설정을 가진다는 의미 (서킷 브레이커 글로벌 설정)
                    id -> {

                        // logging 추가
                        reactiveResilience4JCircuitBreakerFactory.addCircuitBreakerCustomizer(getEventLogger(), id);

                        return new Resilience4JConfigBuilder(id)
                                .circuitBreakerConfig(
                                        CircuitBreakerConfig.ofDefaults() // 기본 설정으로 빌드
                                ).build();
                    }
            );
        };
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> miniFactory() {

        // 서킷 브레이커 설정
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 실패가 50% 비율이 되면 close 에서 open 상태로 변경
                .slidingWindowSize(4) // sliding window 4
                .build();

        // 아래 Id 를 가진 서킷브레이커에 적용할 것이다. (서킷 브레이커가 생성되진 않음, factory.create 를 통해 생성해서 사용해야함, 예약 설정일 뿐이다.)
        String[] targetCircuitBreakerIds = new String[]{"mini"};

        return reactiveResilience4JCircuitBreakerFactory -> {
            // logging 추가
            reactiveResilience4JCircuitBreakerFactory.addCircuitBreakerCustomizer(getEventLogger(), targetCircuitBreakerIds);
            // 서킷 브레이커 설정
            reactiveResilience4JCircuitBreakerFactory.configure(
                    resilience4JConfigBuilder -> resilience4JConfigBuilder.circuitBreakerConfig(circuitBreakerConfig),
                    targetCircuitBreakerIds
            );
        };
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> autoHalfFactory() {

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // failureRate 50 % 가 되면 close 에서 open 으로 변경됨, (sliding window size 가 4 이므로 2 회 실패시.. 변경됨)
                .slidingWindowSize(4) // sliding window size 4
                .enableAutomaticTransitionFromOpenToHalfOpen() // open 상태에서 half-open 상태로 일정 시간(5초) 가 지나면 변경됨.
                .waitDurationInOpenState(Duration.ofSeconds(5)) // open 상태에서 5초 지나면 half-open 상태가 된다.
                .build();

        // 아래 Id 를 가진 서킷브레이커에 적용할 것이다. (서킷 브레이커가 생성되진 않음, factory.create 를 통해 생성해서 사용해야함, 예약 설정일 뿐이다.)
        String[] targetCircuitBreakerIds = new String[]{"autoHalf"};

        return reactiveResilience4JCircuitBreakerFactory -> {

            // logging 추가
            reactiveResilience4JCircuitBreakerFactory.addCircuitBreakerCustomizer(getEventLogger(), targetCircuitBreakerIds);
            // 서킷 브레이커 설정
            reactiveResilience4JCircuitBreakerFactory.configure(
                    resilience4JConfigBuilder -> resilience4JConfigBuilder.circuitBreakerConfig(circuitBreakerConfig),
                    targetCircuitBreakerIds
            );
        };
    }
}
