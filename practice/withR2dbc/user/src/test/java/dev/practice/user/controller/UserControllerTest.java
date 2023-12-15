package dev.practice.user.controller;

import dev.practice.user.service.AuthService;
import dev.practice.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @WebFluxTest 는 servlet stack 의 @WebMvcTest 와 유사하다.
 * controllers 로 명시하지 않으면 default 는 모든 컨트롤러에 대해 진행되어버림
 * Controller advice, filter 등 controller 에 꼭 필요한 빈들이 Spring container 에 등록되고 실행된다.
 */
@WebFluxTest(controllers = UserController.class)
class UserControllerTest {

    /**
     * Slice Test (특정 레이어의 Test 를 할때 사용됨, ex. controller, repository)
     */

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService mockUserService; // UserController 가 의존하는 중

    // @WebFluxTest 는 Filter 도 등록한다. 따라서, 사용자가 만든 SecurityWebFilter 도 등록되어야함.
    // SecurityWebFilter 는 AuthService 를 의존한다.
    @MockBean
    private AuthService mockAuthService;

    @DisplayName("smoke test")
    @Test
    void webTestClient_should_not_be_null() {
        assertNotNull(webTestClient);
    }

    @Nested // UserService::getUserById 메서드 테스트 전용
    class GetUserById {

        @DisplayName("X-I-AM header 토큰이 없다면, 401 에러이다.")
        @Test
        void when_iam_token_is_not_given_then_returns_unauthorized_status() {
            // SecurityWebFilter 테스트 내용이다.

            // given
            Long userId = 1L;

            // when
            // then
            webTestClient.get()
                    .uri("/api/users/{userId}", userId)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @DisplayName("X-I-AM 헤더 토큰으로 AuthService 를 통해 조회 시, 결과가 없다면 401 에러이다.")
        @Test
        void when_auth_service_returns_empty_then_returns_unauthorized_status() {
            // SecurityWebFilter 테스트 내용이다.

            // given
            Long userId = 1L;
            String invalidToken = "invalid-token";

            // stubbing
            when(mockAuthService.getNameByToken(any()))
                    .thenReturn(Mono.empty());

            // when
            // then
            webTestClient.get()
                    .uri("/api/users/{userId}", userId)
                    .header("X-I-AM", invalidToken)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @DisplayName("X-I-AM 헤더 토큰으로 조회한 userId 와 path variable 을 비교해서 다르면 401 에러이다.")
        @Test
        void when_authentication_name_is_not_matched_then_returns_unauthorized_status() {

            // given
            Long userId = 1L;
            String token = "token";

            // stubbing
            when(mockAuthService.getNameByToken(eq(token)))
                    .thenReturn(Mono.just("2"));

            // when
            // then
            webTestClient.get()
                    .uri("/api/users/{userId}", userId)
                    .header("X-I-AM", token)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();
        }

        @DisplayName("userId 로 userService::findById 메서드 호출 했는데 결과가 없으면 404 에러이다.")
        @Test
        void when_user_service_returns_empty_then_returns_not_found_status() {

            // given
            Long userId = 1L;
            String token = "token";

            // stubbing
            when(mockAuthService.getNameByToken(eq(token)))
                    .thenReturn(Mono.just(String.valueOf(userId)));
            when(mockUserService.findById(eq(userId)))
                    .thenReturn(Mono.empty());

            // when
            // then
            webTestClient.get()
                    .uri("/api/users/{userId}", userId)
                    .header("X-I-AM", token)
                    .exchange()
                    .expectStatus()
                    .isNotFound();
        }

        @DisplayName("모든 조건이 부합하면, UserResponse 가 반환된다.")
        @Test
        void when_all_conditions_are_perfect_then_returns_user_resp() {

            // given


            // stubbing
            // when
            // then
        }
    }
}