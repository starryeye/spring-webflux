package dev.practice.circuitbreaker.config;

import io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadAutoConfiguration;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JAutoConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JAutoConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration(
        classes = {
                ReactiveResilience4JAutoConfiguration.class,
                Resilience4JAutoConfiguration.class,
                CircuitBreakerAutoConfiguration.class,
                TimeLimiterAutoConfiguration.class,
                BulkheadAutoConfiguration.class
        }
)
public @interface AutoConfigureReactiveCircuitBreaker {
    /**
     * Test 환경이..
     * 비어있는 Spring Container 로 진행한다.
     * - GreetingService 만 따로 등록한다.
     * - Reactive 환경에서 CircuitBreaker 사용하며 Resilience4j 구현체에 필요한 의존성을 자동으로 등록하도록하는 어노테이션
     */
}
