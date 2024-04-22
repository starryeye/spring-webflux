package dev.practice.mapvsflatmap.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HelloWorldServiceTest {

    @Autowired
    private HelloWorldService helloWorldService;

    private MockWebServer mockHelloWebServer;
    private MockWebServer mockWorldWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockHelloWebServer = new MockWebServer();
        mockHelloWebServer.start(8090);
        mockWorldWebServer = new MockWebServer();
        mockWorldWebServer.start(8091);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockHelloWebServer.shutdown();
        mockWorldWebServer.shutdown();
    }

    @DisplayName("스레드가 예상한 것 처럼 동작하는가?")
    @Test
    void test() {

        // given
        mockHelloWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody("Hello")
        );
        mockWorldWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody("world")
        );

        // when
        Mono<String> result = helloWorldService.collect();

        // then
        StepVerifier.create(result)
                .expectNext("Hello, world")
                .verifyComplete();
    }

}