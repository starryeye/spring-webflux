package dev.practice.gateway.filters;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@ActiveProfiles("gatewayfilter-responseheader")
@AutoConfigureWebTestClient
@SpringBootTest
public class ResponseHeaderGatewayFilterTest {

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
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 AddResponseHeader GatewayFilter")
    @Test
    void test() {

        /**
         * [13]
         * application-gatewayfilter-responseheader.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/add)
         * 3. predicate 가 만족하므로 AddResponseHeader 가 동작 예정(X-Test 헤더에 hello value 추가), 응답 헤더에 적용된다.
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok, 주어진 헤더(X-Test:world) 가 응답으로 내려온다.
         * 6. 3번에서 적용 예정이던 AddResponseHeader gatewayFilter 가 동작하여 X-Test 헤더에 hello value 가 추가되고(덮어쓰기 X) 최종응답으로 내린다.
         * 7. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본으로 200 ok 응답
                        .setHeader("X-Test", "world") // MockWebServer(target) 에서 응답으로 X-Test 헤더에 world 가 채워져서 내려옴
        );

        // when
        // then
        String[] expectHeaderValues = new String[]{"world", "hello"};
        webTestClient.get()
                .uri("/add")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Test", expectHeaderValues);
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 SetResponseHeader GatewayFilter")
    @Test
    void test2() {

        /**
         * [13]
         * application-gatewayfilter-responseheader.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/set)
         * 3. predicate 가 만족하므로 SetResponseHeader 가 동작 예정(X-Test 헤더에 hello value 덮어쓰기), 응답 헤더에 적용된다.
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok, 주어진 헤더(X-Test:world) 가 응답으로 내려온다.
         * 6. 3번에서 적용 예정이던 SetResponseHeader gatewayFilter 가 동작하여 X-Test 헤더에 hello value 가 덮어쓰기 된다.
         * 7. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본으로 200 ok 응답
                        .setHeader("X-Test", "world") // MockWebServer(target) 에서 응답으로 X-Test 헤더에 world 가 채워져서 내려옴
        );

        // when
        // then
        webTestClient.get()
                .uri("/set")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Test", "hello");
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 RewriteResponseHeader GatewayFilter")
    @Test
    void test3() {

        /**
         * [13]
         * application-gatewayfilter-responseheader.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/rewrite)
         * 3. predicate 가 만족하므로 RewriteResponseHeader 가 동작 예정(X-Test 헤더에 h.+ value 가 있으면 hello 로 대체됨), 응답 헤더에 적용된다.
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok, 주어진 헤더(X-Test:hhhh) 가 응답으로 내려온다.
         * 6. 3번에서 적용 예정이던 RewriteResponseHeader gatewayFilter 가 동작하여 X-Test 헤더에 h.+ value 가 있으면 hello 로 대체되고 최종 응답으로 내려감
         * 7. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본으로 200 ok 응답
                        .setHeader("X-Test", "hhhh") // MockWebServer(target) 에서 응답으로 X-Test 헤더에 hhhh 가 채워져서 내려옴
        );

        // when
        // then
        webTestClient.get()
                .uri("/rewrite")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Test", "hello");
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 RemoveResponseHeader GatewayFilter")
    @Test
    void test4() {

        /**
         * [13]
         * application-gatewayfilter-responseheader.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/remove)
         * 3. predicate 가 만족하므로 RemoveResponseHeader 가 동작 예정(X-Test 헤더가 삭제), 응답 헤더에 적용된다.
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok, 설정한 헤더가 포함되어 응답으로 내려온다.
         * 6. 3번에서 적용 예정이던 RemoveResponseHeader gatewayFilter 가 동작하여 X-Test 헤더가 삭제되어 최종 응답으로 내려감
         * 7. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본으로 200 ok 응답
                        .setHeader("X-Test", "hhhh") // MockWebServer(target) 에서 응답으로 X-Test 헤더에 hhhh 가 채워져서 내려옴
        );

        // when
        // then
        webTestClient.get()
                .uri("/remove")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().doesNotExist("X-Test");
    }
}
