package dev.practice.gateway.filters;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
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

@ActiveProfiles("gatewayfilter-setstatus")
@AutoConfigureWebTestClient
@SpringBootTest
public class SetStatusGatewayFilterTest {

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

    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 SetStatus GatewayFilter")
    @Test
    void test() {

        /**
         * [16]
         * application-gatewayfilter-setstatus.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/created)
         * 3. predicate 가 만족하므로 set-ok-status 가 동작 예정
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 3번에서 적용 예정이던 SetStatus gatewayFilter 가 동작하여 Http status code 가 201 로 셋팅되어 최종응답으로 내린다.
         * 7. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본 200 ok 응답
        );

        // when
        // then
        webTestClient.get()
                .uri("/created")
                .exchange()
                .expectStatus().isCreated(); // 201 로 변환되어 응답능
    }

    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 SetStatus GatewayFilter")
    @Test
    void test2() {

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본 200 ok 응답
        );

        // when
        // then
        webTestClient.get()
                .uri("/bad-request")
                .exchange()
                .expectStatus().isBadRequest(); // 400 로 변환되어 응답능
    }
}
