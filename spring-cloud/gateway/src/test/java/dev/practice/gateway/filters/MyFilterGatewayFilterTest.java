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

@ActiveProfiles("gatewayfilter-myfilter")
@AutoConfigureWebTestClient
@SpringBootTest
public class MyFilterGatewayFilterTest {

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
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는게 아니라 직접만든 MyFilter GatewayFilter")
    @Test
    void test() {

        // PrefixPath 와 동작이 동일함

        // given
        // stubbing
        mockWebServer.enqueue(
                new MockResponse() // 기본 200 ok 응답
        );

        // when
        // then
        webTestClient.get()
                .uri("/good/morning")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertEquals("/hello/good/morning", path);
    }
}
