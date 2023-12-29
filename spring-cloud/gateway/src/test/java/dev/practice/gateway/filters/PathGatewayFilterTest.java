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

@ActiveProfiles("gatewayfilter-path")
@AutoConfigureWebTestClient
@SpringBootTest
public class PathGatewayFilterTest {

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
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 PrefixPath GatewayFilter")
    @Test
    void test() {

        /**
         * [17]
         * application-gatewayfilter-path.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/prefix)
         * 3. predicate 가 만족하므로 prefix-path 가 동작 (mockWebServer 로 요청 보낼때 설정한 path 에 prefix 로 /hello 가 붙는다.)
         * 4. gateway 에서 localhost:8001/prefix 로 요청을 보낸다.
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본 200 ok 응답
        );

        // when
        webTestClient.get()
                .uri("/prefix") // client 는 gateway 에 {gateway Domain Url}/prefix 로 요청보냄
                .exchange()
                .expectStatus().isOk();

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertEquals("/hello/prefix", path); // "{gateway Domain Url}/prefix" 로 요청 보냈는데 PrefixPath gatewayFilter 에 의해 "http://localhost:8001/hello/prefix" 로 요청 보내게 되었다.
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 StripPrefix GatewayFilter")
    @Test
    void test2() {

        /**
         * [17]
         * application-gatewayfilter-path.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/strip/**)
         * 3. predicate 가 만족하므로 strip-path 가 동작 (mockWebServer 로 요청 보낼때 설정한 path 에 StripPath 값인 3 개의 prefix 가 삭제된다.)
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다. (prefix 3 개 삭제)
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본 200 ok 응답
        );

        // when
        webTestClient.get()
                .uri("/strip/a/b/c/d") // client 는 gateway 에 {gateway Domain Url}/strip/a/b/c/d 로 요청보냄
                .exchange()
                .expectStatus().isOk();

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertEquals("/c/d", path); // "{gateway Domain Url}/strip/a/b/c/d" 로 요청 보냈는데 StripPath gatewayFilter 에 의해 "http://localhost:8001/c/d" 로 요청 보내게 되었다.
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 SetPath GatewayFilter")
    @Test
    void test3() {

        /**
         * [17]
         * application-gatewayfilter-path.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/set/{segment})
         * 3. predicate 가 만족하므로 set-path 가 동작 (mockWebServer 로 요청 보낼때 설정한 path 에 "{segment}" 부분을 빼서 SetPath 에서 설정한 /hello/{segment} 로 넣고 요청한다.)
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다. (path 는 변경)
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본 200 ok 응답
        );

        // when
        webTestClient.get()
                .uri("/set/abc") // client 는 gateway 에 {gateway Domain Url}/set/abc 로 요청보냄
                .exchange()
                .expectStatus().isOk();

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertEquals("/hello/abc", path); // "{gateway Domain Url}/set/abc" 로 요청 보냈는데 SetPath gatewayFilter 에 의해 "http://localhost:8001/hello/abc" 로 요청 보내게 되었다.
    }

    @SneakyThrows
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 RewritePath GatewayFilter")
    @Test
    void test4() {

        /**
         * [17]
         * application-gatewayfilter-path.yml 에 의해 spring cloud gateway 가 동작함.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate 를 보고 검증 (이 Test 에서는 Path predicate 를 만족하도록 함, Path=/rewrite/**)
         * 3. predicate 가 만족하므로 rewrite-path 가 동작 (mockWebServer 로 요청 보낼때 설정한 path 에 rewrite 이후 부분을 빼서 RewritePath 에서 설정한 /hello 와 /world 사이로 넣고 요청한다.)
         * 4. gateway 에서 localhost:8001 로 요청을 보낸다. (path 는 변경)
         * 5. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok 가 응답으로 내려온다.
         * 6. 검증
         */

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본 200 ok 응답
        );

        // when
        webTestClient.get()
                .uri("/rewrite/abc/123") // client 는 gateway 에 {gateway Domain Url}/set/abc 로 요청보냄
                .exchange()
                .expectStatus().isOk();

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertEquals("/hello/abc/123/world", path); // "{gateway Domain Url}/rewrite/abc/123" 로 요청 보냈는데 RewritePath gatewayFilter 에 의해 "http://localhost:8001/hello/abc/123/world" 로 요청 보내게 되었다.
    }
}
