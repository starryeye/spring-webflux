package dev.practice.gateway.filters;

import dev.practice.gateway.config.CircuitBreakerTestConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ActiveProfiles(
        {
                "gatewayfilter-circuitbreaker",
                "circuitbreaker"
        }
)
@Import(CircuitBreakerTestConfig.class)
@AutoConfigureWebTestClient
@SpringBootTest
public class CircuitBreakerGatewayFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockWebServer mockWebServer;

    private final String SUCCESS_MESSAGE = "hello, success!";
    private final String FALLBACK_MESSAGE = "hello, fallback!";

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;


    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8001);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
        circuitBreakerRegistry.circuitBreaker("MyCircuitBreaker")
                .reset();
    }

    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 CircuitBreaker GatewayFilter")
    @Test
    void test() {


        /**
         * [18]
         * application-gatewayfilter-circuitbreaker.yml 에 의해 spring cloud gateway 가 동작함.
         * application-circuitbreaker.yml 에 의해 서킷 브레이커 구현체 resilience 가 MyCircuitBreaker Factory 를 만듬
         * - CircuitBreakerTestConfig.java 도 참조하여 MyCircuitBreaker Factory 만듬
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/hello)
         * 3. predicate 가 만족하므로 CircuitBreaker gatewayFilter 가 동작 (route id : circuitbreaker_route)
         * 4. gateway 에서 localhost:8001/hello 로 요청을 보낸다.
         *
         * 이후 단계는 test code 로 확인
         *
         */

        // given
        int slidingWindowSize = 4; // application-circuitbreaker.yml

        // stubbing
        mockWebServer.setDispatcher( // setDispatcher 를 통해 gateway 로 부터 요청이 오면 어떤 동작을 할지 정해준다.
                new Dispatcher() {
                    @NotNull
                    @Override
                    public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) throws InterruptedException {

                        Thread.sleep(2000); // 2초 대기 후 응답 반환, 현재 테스트에 적용할 MyCircuitBreaker 의 타임아웃은 1초라 서킷브레이커는 응답을 받기 전 연결 끊고 fallback 수행할 것이다.

                        return new MockResponse()
                                .setBody(SUCCESS_MESSAGE);
                    }
                }
        );

        // when
        // then
        IntStream.range(0, slidingWindowSize)
                .forEach(
                        i -> webTestClient.get()
                                .uri("/hello") // gateway 로 요청 보내면 gateway 에 의해 mockWebServer 로 요청을 보낸다. http://localhost:8001/hello (CB 는 최초 상태이므로 close, slidingwindow 4 만큼 close 로 진행)
                                // mockWebServer 에서는 2초 후 응답을 보내려하지만, circuitbreaker timeout 에 걸려서 실패 처리가 되고 gateway 는 fallback 을 수행한다.(forward)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(String.class).isEqualTo(FALLBACK_MESSAGE) // FallbackController 에서 응답한 응답 데이터가 client 로 도착한다.
                ); // 4회 동안 실패 처리가 되어 이후 MyCircuitBreaker 는 open 상태가 된다.

        assertEquals(CircuitBreaker.State.OPEN, circuitBreakerRegistry.circuitBreaker("MyCircuitBreaker").getState()); // Open 상태 검증

        IntStream.range(0, 100)
                .forEach(
                        i -> webTestClient.get()
                                .uri("/hello")
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(String.class).isEqualTo(FALLBACK_MESSAGE)
                ); // open 상태의 cb 는 더이상 mockWebServer 로 요청 보내지 않고 fallback 만을 수행한다.

        assertEquals(4, mockWebServer.getRequestCount()); // 최초 close 상태일 때 4회 보낸 요청만 mockWebServer 로 전달 됨.
    }

    @SneakyThrows
    @DisplayName("http status 에 따라 fallback 수행하도록 함, 서킷 브레이커만의 기본 동작은 아님 CircuitBreaker GatewayFilter 에서 제공")
    @Test
    void test2() {

        /**
         * HTTP Status 에 따라 fallback 이 수행되는지 확인해본다.
         */

        // given
        int slidingWindowSize = 4;


        // stubbing
        List<Integer> mockWebServerResponseStatusCodes = List.of(200, 200, 400, 500);
        mockWebServerResponseStatusCodes.forEach(
                statusCode -> mockWebServer.enqueue(
                        new MockResponse()
                                .setResponseCode(statusCode) // success message 와 함께 status code 를 200, 200, 400, 500 순으로 응답하도록 함
                                .setBody(SUCCESS_MESSAGE)
                )
        );

        // when
        // then
        IntStream.range(0, slidingWindowSize)
                .forEach(
                        i -> {

                            String expectBody = SUCCESS_MESSAGE;
                            if(mockWebServerResponseStatusCodes.get(i) != 200) {
                                expectBody = FALLBACK_MESSAGE;
                            }

                            // 서킷 브레이커는 이전에 사용되지 않았으므로 sliding window size 만큼 close 로 진행된다.
                            webTestClient.get()
                                    .uri("/hello")
                                    .exchange()
                                    .expectStatus().isOk() // mockWebServer 에서 400, 500 으로 내려도 fallback 이 수행되었으므로 gateway 에서 client 로 200 ok 로 내리는듯..
                                    .expectBody(String.class).isEqualTo(expectBody); // mockWebServer 에서 gateway 로 400, 500 이 응답되면 CircuitBreaker GatewayFilter 에 의해 fallback 수행됨
                        }
                );

        assertEquals(4, mockWebServer.getRequestCount()); // close 상태로 진행하였으므로 4 회 모두 요청됨

        // 아래는 해당 Test 와는 관계 없지만 추가 검증
        // 실패율 50% 이상이므로 Open 으로 변경됨
        assertEquals(CircuitBreaker.State.OPEN, circuitBreakerRegistry.circuitBreaker("MyCircuitBreaker").getState());

        log.info("3s init");
        Thread.sleep(3000); // half-open 상태로 전환을 위해 3 초 대기
        log.info("3s end");

        // 3초 지나서 half-open 상태로 변경됨
        assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreakerRegistry.circuitBreaker("MyCircuitBreaker").getState());

        log.info("test end"); // test 가 끝나고 Half-open 에서 close 로 변경되는건.. teardown 메서드의 cb reset 때문이다.
    }
}
