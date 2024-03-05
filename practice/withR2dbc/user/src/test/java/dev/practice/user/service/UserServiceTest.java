package dev.practice.user.service;

import dev.practice.user.client.ImageHttpClient;
import dev.practice.user.client.ImageResponse;
import dev.practice.user.common.domain.Image;
import dev.practice.user.common.domain.User;
import dev.practice.user.common.repository.UserEntity;
import dev.practice.user.repository.UserR2dbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    /**
     * Unit Test (Domain test, 비즈니스 테스트에 적합)
     */

    @InjectMocks
    UserService userService;

    @Mock
    AuthService mockAuthService;

    @Mock
    UserR2dbcRepository mockUserR2dbcRepository;

    @Mock
    ImageHttpClient mockImageHttpClient;


    @Nested
    class FindById {

        Long userId;

        @BeforeEach
        void setup() {
            userId = 1L;
        }


        @DisplayName("찾을 수 없는 userId 로 User 를 찾으면 Mono.empty 가 반환된다.")
        @Test
        void when_user_repository_returns_empty_then_returns_empty_mono() {

            // given
            // stubbing
            when(mockUserR2dbcRepository.findById(eq(userId)))
                    .thenReturn(Mono.empty());

            // when
            Mono<User> result = userService.findById(userId);

            // then
            StepVerifier.create(result)
                    .verifyComplete();
        }

        @Nested // user 는 정상적으로 찾아지는 case 모음
        class UserIsFound {

            UserEntity givenUser;

            @BeforeEach
            void setup() {
                givenUser = UserEntity.createWithId(userId, "test", 20, "1", "2");

                // stubbing
                when(mockUserR2dbcRepository.findById(eq(userId)))
                        .thenReturn(Mono.just(givenUser));
            }

            @DisplayName("user 는 찾았지만, image 를 찾지 못하면 user 내부 image 는 empty image 가 된다.")
            @Test
            void when_image_is_empty_then_returns_user_with_empty_image() {

                // given
                // stubbing
                when(mockImageHttpClient.getImageResponseByImageId(givenUser.getProfileImageId()))
                        .thenReturn(Mono.empty());

                // when
                Mono<User> result = userService.findById(givenUser.getId());

                // then
                StepVerifier.create(result)
                        .assertNext(
                                foundUser -> {

                                    assertTrue(foundUser.getProfileImage().isEmpty());

                                    assertEquals(givenUser.getId(), foundUser.getId());
                                    assertEquals(givenUser.getAge(), foundUser.getAge());
                                    assertEquals(givenUser.getName(), foundUser.getName());
                                }
                        ).verifyComplete();
            }

            @DisplayName("user, image 를 모두 찾으면 user 내부 image 는 찾은 image 가 들어가 있다.")
            @Test
            void when_image_is_not_empty_then_returns_user_with_not_empty_image() {

                // given
                ImageResponse givenImageResponse = new ImageResponse(
                        givenUser.getProfileImageId(),
                        "test's profileImage",
                        "https://practice.dev/images/1"
                );

                // stubbing
                when(mockImageHttpClient.getImageResponseByImageId(givenUser.getProfileImageId()))
                        .thenReturn(Mono.just(givenImageResponse));

                // when
                Mono<User> result = userService.findById(givenUser.getId());

                // then
                StepVerifier.create(result)
                        .assertNext(
                                foundUser -> {

                                    assertTrue(foundUser.getProfileImage().isPresent());
                                    Image image = foundUser.getProfileImage().orElseThrow();
                                    assertEquals(image.getId(), givenImageResponse.getId());
                                    assertEquals(image.getName(), givenImageResponse.getName());
                                    assertEquals(image.getUrl(), givenImageResponse.getUrl());

                                    assertEquals(foundUser.getId(), givenUser.getId());
                                    assertEquals(foundUser.getAge(), givenUser.getAge());
                                    assertEquals(foundUser.getName(), givenUser.getName());
                                }
                        ).verifyComplete();
            }
        }
    }
}