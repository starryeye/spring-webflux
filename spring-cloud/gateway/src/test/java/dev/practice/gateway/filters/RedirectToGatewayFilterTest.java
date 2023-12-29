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

@ActiveProfiles("gatewayfilter-redirectto")
@AutoConfigureWebTestClient
@SpringBootTest
public class RedirectToGatewayFilterTest {

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

    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 RedirectTo GatewayFilter")
    @Test
    void test() {

        /**
         * [14]
         * application-gatewayfilter-redirectto.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/redirect)
         * 3. predicate 가 만족하므로 RedirectTo gatewayFilter 가 동작 예정
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok, 설정한 헤더가 포함되어 응답으로 내려온다.
         * 6. 3번에서 적용 예정이던 RedirectTo gatewayFilter 가 동작하여 MockWebServer 에서 무슨 응답이 오든 간에 Http status code 를 308, location 에 http://practice.dev 를 설정하여 client 로 응답을 보낸다.(redirect 목적)
         * 7. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본은 200 ok
        );

        // when
        // then
        webTestClient.get()
                .uri("/redirect") // predicate 만족
                .exchange()
                .expectStatus().isEqualTo(308) // mockWebServer 에서는 200 ok 를 내렸지만 최종 client 는 308 을 받았다.
                .expectHeader().location("http://practice.dev"); // 마찬가지로 mockWebServer 에서는 location 에 아무런 값을 집어 넣지 않았지만, RedirectTo gatewayFilter 가 동작하여 값이 생겼다.
    }
}
