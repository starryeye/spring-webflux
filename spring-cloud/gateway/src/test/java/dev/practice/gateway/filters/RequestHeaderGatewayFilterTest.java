package dev.practice.gateway.filters;

import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("gatewayfilter-requestheader")
@AutoConfigureWebTestClient
@SpringBootTest
public class RequestHeaderGatewayFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8001);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 AddRequestHeader GatewayFilter")
    @Test
    void test() {

        //todo, 원래 되어야 하지만.. UnsupportedOperationException 예외가 발생한다. 원인은 기존 헤더에 헤더 값 추가를 못하는듯

        /**
         * [12]
         * application-gatewayfilter-requestheader.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/add)
         * 3. predicate 가 만족하므로 AddRequestHeader 가 동작 (X-Test 헤더에 hello value 추가)
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // MockWebServer 의 기본 동작은 200 ok 응답
        );

        // when
        webTestClient.get()
                .uri("/add") // predicate 만족
                .header("X-Test", "world") // 기존에는 X-Test 헤더에 world 가 존재 (없어도 동작하는데 문제 없음)
                .exchange()
                .expectStatus().isOk();

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        List<String> requestHeaderValues = request.getHeaders().values("X-Test");
        assertIterableEquals(List.of("world", "hello"), requestHeaderValues); // AddRequestFilter 에 의해 gateway 에서 mockWebServer 로 보내는 request 헤더에 K: X-Test, V: hello 가 추가됨(덮어쓰기X)
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 SetRequestHeader GatewayFilter")
    @Test
    void test2() {

        /**
         * [12]
         * application-gatewayfilter-requestheader.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/set)
         * 3. predicate 가 만족하므로 SetRequestHeader 가 동작 (X-Test 헤더에 world value 덮어쓰기)
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // MockWebServer 의 기본 동작은 200 ok 응답
        );

        // when
        webTestClient.get()
                .uri("/set") // predicate 만족
                .header("X-Test", "hello") // 기존에는 X-Test 헤더에 hello 로 되어있음
                .exchange()
                .expectStatus().isOk();

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        String requestHeaderValue = request.getHeader("X-Test");
        assertEquals("world", requestHeaderValue); // SetRequestFilter 에 의해 gateway 에서 mockWebServer 로 보내는 request 헤더에 K: X-Test, V: world 가 덮어쓰기됨
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 MapRequestHeader GatewayFilter")
    @Test
    void test3() {

        /**
         * [12]
         * application-gatewayfilter-requestheader.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/map)
         * 3. predicate 가 만족하므로 MapRequestHeader 가 동작 (X-Test 헤더값이 X-Test-Copy 헤더값으로 카피)
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // MockWebServer 의 기본 동작은 200 ok 응답
        );

        // when
        webTestClient.get()
                .uri("/map") // predicate 만족
                .header("X-Test", "hello")
                .exchange()
                .expectStatus().isOk();

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        String requestHeaderValue = request.getHeader("X-Test-Copy");
        assertEquals("hello", requestHeaderValue); // SetRequestFilter 에 의해 gateway 에서 mockWebServer 로 보내는 request 헤더에 K: X-Test-Copy, V: hello 가 추가됨 (X-Test 카피)
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 RemoveRequestHeader GatewayFilter")
    @Test
    void test4() {

        /**
         * [12]
         * application-gatewayfilter-requestheader.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/remove)
         * 3. predicate 가 만족하므로 RemoveRequestHeader 가 동작 (X-Test 헤더가 삭제됨)
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // MockWebServer 의 기본 동작은 200 ok 응답
        );

        // when
        webTestClient.get()
                .uri("/remove") // predicate 만족
                .header("X-Test", "hello")
                .exchange()
                .expectStatus().isOk();

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        String requestHeaderValue = request.getHeader("X-Test");
        assertNull(requestHeaderValue); // SetRequestFilter 에 의해 gateway 에서 mockWebServer 로 보내는 request X-Test 헤더가 삭제됨
    }
}
