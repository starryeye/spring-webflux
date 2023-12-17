package dev.practice.user;

import dev.practice.user.common.repository.AuthEntity;
import dev.practice.user.common.repository.UserEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ActiveProfiles("integration")
@AutoConfigureWebTestClient // @SpringBootTest 는 WebTestClient 를 자동으로 등록하지 않아서 해당 어노테이션을 사용함
@SpringBootTest(
        properties = { // WebConfig 를 통해 WebClient 를 localhost:10000 으로 설정
                "image.server.url=http://localhost:10000"
        }
)
public class UserIntegrationTest {

    /**
     * Integration Test 이다.
     * <p>
     * 실제 실행 환경과 가장 유사하게 진행한다.
     * <p>
     * 통합테스트는 수행 비용이 크고 경우의 수도 많아서 때문에 happy test 를 위주로 수행하고
     * 정말 중요한 test 가 있을 경우에 추가하는 방식으로 수행한다.
     */

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    // 외부 API Server 를 Mock 한다. (image server 가 타겟)
    private MockWebServer mockWebServer;
    private final Integer mockWebServerPort = 10000;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        mockWebServer.start(mockWebServerPort); // MockWebServer 를 localhost:10000 로 설정
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        mockWebServer.close();
    }

    @DisplayName("smoke test")
    @Test
    void contextLoad() {
    }

    @Nested
    class GetUserById {

        @DisplayName("통합 테스트, 정상 요청하면 정상 응답을 줘야한다.")
        @Test
        void when_under_perfect_conditions_then_returns_user_resp() {

            // given
            String imageId = "123";
            UserEntity userEntity = UserEntity.create(
                    "tester",
                    10,
                    imageId,
                    "123123"
            );

            UserEntity savedUser = r2dbcEntityTemplate.insert(userEntity)
                    .block(); // test 용 userEntity 를 db 에 저장

            Long userId = Optional.ofNullable(savedUser).orElseThrow()
                    .getId();

            String token = "test-token";
            AuthEntity authEntity = AuthEntity.create(userId, token);

            r2dbcEntityTemplate.insert(authEntity)
                    .block(); // test 용 authEntity 를 db 에 저장

            // stubbing
            mockWebServer.enqueue(
                    new MockResponse()
                            .setHeader("Content-Type", "application/json")
                            .setBody(
                                    "{\"id\": \"123\", \"name\": \"tester's profile\", \"url\": \"http://practice.dev/images/123\"}"
                            )
            ); // image server api 요청에 대한 응답 stubbing

            // when
            // then
            EntityExchangeResult<byte[]> result = webTestClient.get()
                    .uri("/api/users/{userId}", userId)
                    .header("X-I-AM", token)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(userId)
                    .jsonPath("$.id.imageResponse").isEqualTo(imageId)
                    .returnResult();

            log.info("result = {}", result);

            assertEquals(1, mockWebServer.getRequestCount());
        }
    }
}
