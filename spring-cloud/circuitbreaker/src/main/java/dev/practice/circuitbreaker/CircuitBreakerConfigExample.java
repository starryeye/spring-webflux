package dev.practice.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class CircuitBreakerConfigExample {

    /**
     * 서킷 브레이커 예약(?) 설정이다.
     * 서킷 브레이커 factory 를 만들어 bean 으로 등록한다고 생각하자..
     *
     * -> 해당 factory 로 사용자는 서킷 브레이커를 만들 수 있고..
     *
     * -> 특정 factory 를 만들면서 서킷 브레이커 이름을 적어놓고..
     * -> 적어놓은 서킷 브레이커 이름으로 factory 를 통해 서킷 브레이커를 만들면 특정 설정이 적용된 서킷 브레이커를 만들 수 있다.
     *
     *
     * 아래 bean 들은 해당 프로젝트에서 전혀 사용되지 않고 있다.
     * Test 에서 사용된 factory 는 TestCircuitBreakerConfig.java 를 참고하자.
     *
     * 빈 등록 방식 말고..
     * yaml 로 factory 를 만들어 낼 수 도 있다. (물론 해당 프로젝트에서는 사용되지 않고 있다.)
     *
     */

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultFactory() {

        // 글로벌 서킷 브레이커 설정

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
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> fullFactory() {

        // 특정 서킷 브레이커 대상 설정

        // 주요한 설정을 다 해보는 예제 설정이다.

        // circuitBreakerConfig, 서킷 브레이커에 대한 설정이다.
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(10) // 실행 수 총량, close 상태에서 failureRate 에 도달하면 open 상태가 된다. (기본 값 100)
                .failureRateThreshold(75) // slidingWindowSize 와 permittedNumberOfCallsInHalfOpenState 에서 몇 퍼센트 (75%) 이면 실패로 보고 open 으로 전환 할 것인가. (기본 값 50)
                .enableAutomaticTransitionFromOpenToHalfOpen() // open 상태에서 특정 시간이 지나면 half-open 상태로 전환 하는 설정 (기본 값 켜져있지 않음)
                .waitDurationInOpenState(Duration.ofSeconds(5)) // open 상태에서 5초 지나면 half-open 상태로 전환한다. (기본 값 60)
                .permittedNumberOfCallsInHalfOpenState(6) // 검증 수 총량, half-open 상태에서 failureRate 에 도달하면 open 상태가 된다. (기본 값 10)
                .ignoreExceptions(ArithmeticException.class) // 서킷브레이커에서 수행한 publisher 에서 밣생한 모든 예외는 catch 되고 throw 되지 않고 fallback 이 반환되는데.. 이를 무시할 exception 을 설정한다.
                .maxWaitDurationInHalfOpenState(Duration.ofSeconds(30)) // half-open 상태에서 permitted 값이 너무 크거나 요청 수행 자체가 적게 수행된다면 half-open 상태가 오랜기간 지속된다. 특정 시간(30) 동안 제대로된 검증(permitted 가 채워짐)이 수행 되지 않으면, half-open 상태에서 open 상태로 변경한다. (기본 값 0)
                .build();

        // timeLimiterConfig, 서킷 브레이커의 타임 아웃 설정이다.
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .cancelRunningFuture(true) // publisher(요청) 를 취소할 수 있도록 설정 (기본 값 true)
                .timeoutDuration(Duration.ofSeconds(3)) // publisher(요청) 를 수행 후 몇 초 기다리면 timeout 으로 볼 것인가.. (기본 값 1)
                .build();

        // "example" 이라는 circuitBreaker id 를 가진 서킷 브레이커에 해당 설정들을 적용 할 예정이다.
        // 서킷 브레이커가 생성되는 것은 아님, 서킷 브레이커는 ReactiveCircuitBreakerFactory::create 로 직접 생성해줘야한다. (GreeterService 참조)
        String[] targetCircuitBreakerIds = new String[]{"example"};

        // reactiveResilience4JCircuitBreakerFactory 에는 circuitBreakerConfig, timeLimiterConfig 2개의 설정을 넣을 수 있다.
        return reactiveResilience4JCircuitBreakerFactory -> {

            // eventPublisher custom
            reactiveResilience4JCircuitBreakerFactory.addCircuitBreakerCustomizer(getEventLogger(), targetCircuitBreakerIds);
            // circuitBreaker 설정, timeLimiter 설정 으로 reactiveResilience4JCircuitBreakerFactory 반환!
            reactiveResilience4JCircuitBreakerFactory.configure(
                    resilience4JConfigBuilder -> resilience4JConfigBuilder
                            .circuitBreakerConfig(circuitBreakerConfig)
                            .timeLimiterConfig(timeLimiterConfig),
                    targetCircuitBreakerIds
            );
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
