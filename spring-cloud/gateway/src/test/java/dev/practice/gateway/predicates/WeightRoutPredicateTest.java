package dev.practice.gateway.predicates;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.IntStream;

@Slf4j
@ActiveProfiles("predicate-weight")
@AutoConfigureWebTestClient
@SpringBootTest
public class WeightRoutPredicateTest {

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
    @DisplayName("Spring cloud gateway 에서 기본으로 제공해주는 weight predicate")
    @Test
    void test() {

        /**
         * [10]
         * application-predicate-weight.yml 에 의해 spring cloud gateway 가 동작함.
         * - 가중치 값에 따라 요청을 분산한다.
         *
         * 테스트 시나리오
         * WebTestClient -> gateway -> MockWebServer
         * 1. webTestClient 로 현재 테스트 타겟 서버인 spring cloud gateway 에 요청을 보낸다.
         * 2. gateway 서버에서는 predicate weight 조건에 의해 main or canary 가 동작
         * 3. gateway 에서 weight 가중치 비율로 main or canary 로 선택되어 요청됨 localhost:8001/branch/main or localhost:8001/branch/canary 로 요청을 보낸다. (필터에서 조작이 없으므로 webTestClient 에서 도메인만 변경된 상태로 그대로 요청)
         * 4. MockWebServer 를 사용하여 mocking 된 서버에 stubbing 된 것과 같이 200 ok, message 가 응답으로 내려온다.
         * 5. gateway 의 응답관련 filter 가 없으므로 그대로 최종 webTestClient 로 응답이 내려간다.
         * 6. 검증
         */

        // given
        // stubbing
        int testCount = 1000;
        IntStream.range(0, testCount)
                .forEach(
                        i -> mockWebServer.enqueue(new MockResponse()) // setResponseCode, setBody 등이 없으면 기본적으로 200 ok 반환
                );

        // when
        // then
        int mainCount = 0;
        for (int i = 0; i < testCount; i++) {

            webTestClient.get()
                    .uri("/")
                    .exchange()
                    .expectStatus().isOk();

            RecordedRequest request = mockWebServer.takeRequest();
            String path = request.getPath();
            if(path.equals("/branch/main")) {
                mainCount++;
            }
        }

        log.info("/branch/main request count : {}", mainCount); // 대충 99 비율
        log.info("/branch/canary request count : {}", testCount - mainCount); // 대충 1 비율

    }
}
