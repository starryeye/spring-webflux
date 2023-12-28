package dev.practice.gateway.predicates;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("predicate-header")
@AutoConfigureWebTestClient
@SpringBootTest
public class HeaderRoutePredicateTest {

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


    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 header predicate")
    @Test
    void test() {

        /**
         * [5]
         * application-predicate-header.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate header 조건에 의해 greeting-header(id) 가 동작, header 에 K: x-greeting, V: ^H.* 가 존재하는지 검사
         * 3. header 가 존재하면, gateway 에서 localhost:8001 로 요청을 보낸다. (필터에서 조작이 없으므로 webTestClient 에서 도메인만 변경된 상태로 그대로 요청)
         * 4. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok, message 가 응답으로 내려온다.
         * 5. gateway 의 응답관련 filter 가 없으므로 그대로 최종 webTestClient 로 응답이 내려간다.
         * 6. 검증
         */

        // given
        // stubbing
        String message = "hello, world!";
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(message)
        );

        // when
        // then
        webTestClient.get()
                .uri("/")
                .header("x-greeting", "Hello")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("hello, world!");
    }

    @DisplayName("header 존재 하지 않는 요청을 보내면 not found")
    @Test
    void test2() {

        // given
        // stubbing
        String message = "hello, world!";
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(message)
        );

        // when
        // then
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isNotFound();

        assertEquals(0, mockWebServer.getRequestCount()); // gateway 에서 MockWebServer 서버로 요청을 보내지 않음
    }

    @DisplayName("header 는 존재하지만, 값 조건이 불만족하는 요청을 보내면 not found")
    @Test
    void test3() {

        // given
        // stubbing
        String message = "hello, world!";
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(message)
        );

        // when
        // then
        webTestClient.get()
                .uri("/")
                .header("x-greeting", "AA")
                .exchange()
                .expectStatus().isNotFound();

        assertEquals(0, mockWebServer.getRequestCount()); // gateway 에서 MockWebServer 서버로 요청을 보내지 않음
    }
}
