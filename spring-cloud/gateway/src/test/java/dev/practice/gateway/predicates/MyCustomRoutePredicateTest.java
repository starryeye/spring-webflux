package dev.practice.gateway.predicates;

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

@ActiveProfiles("predicate-mycustom")
@AutoConfigureWebTestClient
@SpringBootTest
public class MyCustomRoutePredicateTest {

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
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 predicate 가 아니라 직접 만든 predicate")
    @Test
    void test() {

        /**
         * [11]
         * application-predicate-mycustom.yml 에 의해 spring cloud gateway 가 동작함.
         * - MyCustomRoutePredicateFactory.java 에 apply 메서드 조건을 따름
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate MyCustom 조건에 의해 hello(id) 가 동작, request uri path 에 주어진 greeting 값이 존재하는지 확인
         * 3. 존재하면, gateway 에서 localhost:8001 로 요청을 보낸다. (필터에서 조작이 없으므로 webTestClient 에서 도메인만 변경된 상태로 그대로 요청)
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
                .uri("/hello") // application-predicate-mycustom.yml 의 predicate args greeting 값인 hello 가 request url path 에 포함
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(message);

        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertEquals("/hello", path); // gateway 가 mockWebServer 로 요청한 url 에 "/hello" 가 그대로 포함되어 요청
    }

    @SneakyThrows
    @DisplayName("shortcut 지원 확인")
    @Test
    void test2() {

        /**
         * [11]
         * application-predicate-mycustom.yml 에 의해 spring cloud gateway 가 동작함.
         * - MyCustomRoutePredicateFactory.java 에 apply 메서드 조건을 따름
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate MyCustom 조건에 의해 world(id) 가 동작, request uri path 에 주어진 greeting 값(world)이 존재하는지 확인
         * 3. 존재하면, gateway 에서 localhost:8001 로 요청을 보낸다. (필터에서 조작이 없으므로 webTestClient 에서 도메인만 변경된 상태로 그대로 요청)
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
                .uri("/world") // application-predicate-mycustom.yml 의 predicate MyCustom greeting 값인 "world" 가 request url path 에 포함
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(message);

        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertEquals("/world", path); // gateway 가 mockWebServer 로 요청한 url 에 "/world" 가 그대로 포함되어 요청
    }
}
