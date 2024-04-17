package dev.practice.gateway.globalfilters;

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

@ActiveProfiles("globalfilter")
@AutoConfigureWebTestClient
@SpringBootTest
public class MyCustomGlobalFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockWebServer mockWebServerA;
    private MockWebServer mockWebServerB;
    private MockWebServer mockWebServerC;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServerA = new MockWebServer();
        mockWebServerA.start(8090);

        mockWebServerB = new MockWebServer();
        mockWebServerB.start(8091);

        mockWebServerC = new MockWebServer();
        mockWebServerC.start(8092);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServerA.shutdown();
        mockWebServerB.shutdown();
        mockWebServerC.shutdown();
    }

    @DisplayName("GlobalFilter 로 http body 값에 따라 URI 를 switch 하도록 한다.")
    @Test
    void test() {
        //MyCustomGlobalFilter 를 빈등록 해주고 test 할 것..

        //Change the URI dynamically

        mockWebServerA.enqueue(new MockResponse().setBody("success!"));

        // given
        String requestBody = "AAA";

        // when
        // then
        webTestClient.post()
                .uri("http://localhost:8080") // gateway 는 8080, target 은 8090
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(responseBody -> responseBody.equals("success!"));
    }
}
