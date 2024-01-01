package dev.practice.gateway;

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

@ActiveProfiles("route-example")
@AutoConfigureWebTestClient
@SpringBootTest
public class RouteExampleTest {

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

    @DisplayName("predicate Path Method Header, filter AddResponseHeader SetStatus")
    @Test
    void test1() {

        /**
         * spring cloud gateway 기본 제공 predicate, gatewayFilter 를 조합해서 사용해본다.
         * predicate
         * - Path
         * - Method
         *
         * gatewayFilter
         * - AddResponseHeader
         * - SetStatus
         */

        // given
        mockWebServer.enqueue(
                new MockResponse()
        );

        // when
        // then
        webTestClient.get()
                .uri("/hello")
                .header("X-I-AM", "token")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("X-Hello", "world");
    }

    @SneakyThrows
    @DisplayName("predicate Path, filter RewritePath")
    @Test
    void test2() {

        /**
         * spring cloud gateway 기본 제공 predicate, gatewayFilter 를 조합해서 사용해본다.
         * predicate
         * - Path
         *
         * gatewayFilter
         * - RewritePath
         */

        // given
        mockWebServer.enqueue(
                new MockResponse()
        );

        // when
        // then
        webTestClient.get()
                .uri("/world/abc/def")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals("/abc/def", request.getPath());
    }
}
