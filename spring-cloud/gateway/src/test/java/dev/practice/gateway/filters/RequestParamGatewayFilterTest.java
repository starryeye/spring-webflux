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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("gatewayfilter-requestparam")
@AutoConfigureWebTestClient
@SpringBootTest
public class RequestParamGatewayFilterTest {

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
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 AddRequestParam GatewayFilter")
    @Test
    void test() {

        /**
         * [15]
         * application-gatewayfilter-requsetparam.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/add)
         * 3. predicate 가 만족하므로 AddRequestParam 이 동작 (mockWebServer 로 요청 보낼때 request param 추가, 덮어쓰기 X)
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본으로 200 ok 응답
        );

        // when
        webTestClient.get()
                .uri("/add?greeting=hello") // 기본 request param 으로 greeting hello 로 요청
                .exchange()
                .expectStatus().isOk();

        // then
        RecordedRequest request = mockWebServer.takeRequest(); // gateway 에서 mockWebServer 로 보낸 요청 정보를 획득
        String path = request.getPath();
        assertEquals("/add?greeting=hello&greeting=world", path); // AddRequestParam GatewayFilter 에 의해 mockWebServer 로 요청할 때 greeting world request param 이 추가 되었다.
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 RemoveRequestParam GatewayFilter")
    @Test
    void test2() {

        /**
         * [15]
         * application-gatewayfilter-requsetparam.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/remove)
         * 3. predicate 가 만족하므로 RemoveRequestParam 이 동작 (mockWebServer 로 요청 보낼때 설정한(greeting) request param 이 삭제된다.)
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본으로 200 ok 응답
        );

        // when
        webTestClient.get()
                .uri("/remove?greeting=hello") // 기본 request param 으로 greeting hello 로 요청
                .exchange()
                .expectStatus().isOk();

        // then
        RecordedRequest request = mockWebServer.takeRequest(); // gateway 에서 mockWebServer 로 보낸 요청 정보를 획득
        String path = request.getPath();
        assertEquals("/remove", path); // RemoveRequestParam GatewayFilter 에 의해 mockWebServer 로 요청할 때 greeting request param 이 삭제되었다.
    }
}
