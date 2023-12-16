package dev.practice.user.controller;

import dev.practice.user.common.domain.User;
import dev.practice.user.service.AuthService;
import dev.practice.user.service.UserService;
import lombok.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
            when(mockAuthService.getNameByToken(token))
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
            when(mockAuthService.getNameByToken(token))
                    .thenReturn(Mono.just(String.valueOf(userId)));
            when(mockUserService.findById(userId))
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
            // stubbing 된 user 를 이용해서 UserResponse 가 생성되어 리턴되는지 확인한다.

            // given
            Long userId = 1L;
            String token = "token";
            User user = TestDataBuilderForController.createUser(userId);

            // stubbing
            when(mockAuthService.getNameByToken(token))
                    .thenReturn(Mono.just(String.valueOf(userId))); // token 을 주면 userId 를 리턴 stubbing
            when(mockUserService.findById(userId))
                    .thenReturn(Mono.just(user)); // userId 를 주면 user 를 리턴 stubbing

            // when
            // then
            webTestClient.get()
                    .uri("/api/users/{userId}", userId)
                    .header("X-I-AM", token)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON)
                    .expectBody(UserRes.class)
                    .value(
                            resultBody -> {
                                assertEquals(user.getId(), resultBody.id);
                                assertEquals(user.getAge(), resultBody.age);
                                assertEquals(user.getFollowCount(), resultBody.followCount);

                                assertEquals(user.getProfileImage().orElseThrow().getId(), resultBody.imageResponse.orElseThrow().id);
                                assertEquals(user.getProfileImage().orElseThrow().getName(), resultBody.imageResponse.orElseThrow().name);
                            }
                    );
        }
    }

    @Nested // UserService::signupUser 메서드 테스트 전용
    class SignupUser {

        @DisplayName("SignupUserRequest 로 요청하면 UserResponse 가 응답으로 전달된다.")
        @Test
        void when_signup_request_is_given_then_returns_saved_user() {
            // controller test 이므로 UserService 는 Mock 객체로 진행되고 실제 저장되는지는 확인하지 않는다.

            // given
            String name = "tester";
            Integer age = 20;
            String profileImageId = "1";
            String password = "123123";
            SignupUserReq request = new SignupUserReq(
                    name,
                    age,
                    password,
                    profileImageId
            );

            Long userId = 1L;
            User createdUser = TestDataBuilderForController.createUser(
                    userId,
                    name,
                    age,
                    password,
                    profileImageId
            );

            // stubbing
            when(mockUserService.createUser(
                    anyString(),
                    anyInt(),
                    anyString(),
                    anyString()
            )).thenReturn(Mono.just(createdUser));

            // when
            // then
            webTestClient.post()
                    .uri("/api/users/signup")
                    .bodyValue(request)
                    .exchange()
                    .expectStatus()
                    .isCreated()
                    .expectBody(UserRes.class)
                    .value(
                            resultBody -> {
                                assertEquals(createdUser.getId(), resultBody.id);
                                assertEquals(createdUser.getAge(), resultBody.age);
                                assertEquals(createdUser.getFollowCount(), resultBody.followCount);

                                assertEquals(createdUser.getProfileImage().orElseThrow().getId(), resultBody.imageResponse.orElseThrow().id);
                                assertEquals(createdUser.getProfileImage().orElseThrow().getName(), resultBody.imageResponse.orElseThrow().name);
                            }
                    );
        }

        @DisplayName("userService 에서 Mono.empty 를 반환하면 400 에러를 응답으로 내린다.")
        @Test
        void when_create_user_returns_empty_mono_then_returns_status_400() {

            // given
            String name = "tester";
            Integer age = 20;
            String profileImageId = "1";
            String password = "123123";
            SignupUserReq request = new SignupUserReq(
                    name,
                    age,
                    password,
                    profileImageId
            );

            // stubbing
            when(mockUserService.createUser(
                    anyString(),
                    anyInt(),
                    anyString(),
                    anyString()
            )).thenReturn(Mono.empty());

            // when
            // then
            webTestClient.post()
                    .uri("/api/users/signup")
                    .bodyValue(request)
                    .exchange()
                    .expectStatus()
                    .is4xxClientError();
        }

        @DisplayName("userService 에서 exception 이 발생하면 400 에러를 응답으로 내린다.")
        @Test
        void when_create_user_returns_error_then_returns_status_400() {

            // given
            String name = "tester";
            Integer age = 20;
            String profileImageId = "1";
            String password = "123123";
            SignupUserReq request = new SignupUserReq(
                    name,
                    age,
                    password,
                    profileImageId
            );

            IllegalStateException error = new IllegalStateException("user service error throw..");

            // stubbing
            when(mockUserService.createUser(
                    anyString(),
                    anyInt(),
                    anyString(),
                    anyString()
            )).thenReturn(Mono.error(error));

            // when
            // then
            webTestClient.post()
                    .uri("/api/users/signup")
                    .bodyValue(request)
                    .exchange()
                    .expectStatus()
                    .is4xxClientError();
        }
    }

    /**
     * 아래 private static class 는 실제 UserResponse, ProfileImageResponse 클래스에 대응 되는 Test 용 클래스이다.
     * 실제 클래스를 써도 되긴한다.
     *
     * 실제 객체들이 변경 되었을 때 테스트에서 fail 이 정확하게 일어나도록 인지를 하기 위한 목적으로
     * 중복이지만, 따로 선언해서 사용해보도록 하겠다.
     */
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class UserRes {
        private Long id;
        private String name;
        private Integer age;
        private Long followCount;
        private Optional<ProfileImageRes> imageResponse;
    }

    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class ProfileImageRes {
        private String id;
        private String name;
        private String url;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    private static class SignupUserReq {
        private String name;
        private Integer age;
        private String password;
        private String profileImageId;
    }
}