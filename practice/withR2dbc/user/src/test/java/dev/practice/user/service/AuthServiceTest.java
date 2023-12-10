package dev.practice.user.service;

import dev.practice.user.common.repository.AuthEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// ExtendWith 로 MockitoExtension 을 하여, @Mock, @InjectMocks 등을 사용할 수 있도록 함
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    /**
     * Unit Test (Domain test, 비즈니스 테스트에 적합)
     */

    @Mock
    R2dbcEntityTemplate mockR2dbcEntityTemplate; // prefix 로 mock 을 붙여 Mock 객체임을 강조한다.

    // InjectMocks 를 사용하여 AuthService 를 생성하는데 필요한 의존성 객체(R2dbcEntityTemplate) 가 있을 경우 Mock 으로 집어 넣는다.
    // 위 mockR2dbcEntityTemplate 와 동일한 객체가 주입된다.
    @InjectMocks
    AuthService authService;

    @Mock
    ReactiveSelectOperation.ReactiveSelect<AuthEntity> mockReactiveSelect;

    @Mock
    ReactiveSelectOperation.TerminatingSelect<AuthEntity> mockTerminatingSelect;

    @Captor // ReactiveSelect::matching 파라미터로 전달될 값을 저장하기 위함
    ArgumentCaptor<Query> queryArgumentCaptor;

    @Test
    void authServiceNotNull() {
        assertNotNull(authService); // 잘 생성 되었는지 확인
        assertTrue(MockUtil.isMock(mockR2dbcEntityTemplate)); // Mock 객체인지 확인
    }

    @Nested // Nested 를 통하여, getNameByToken 메서드 테스트하는 공간을 class 로 분리한다.
    class GetNameByToken {

        String token;

        @BeforeEach
        void setup() {

            /**
             * stubbing 은 @Test 내로 옮기는게 가독성에 좋을 듯.
             */

            token = "valid_token";

            // stubbing
            // test 중 아래 두개의 stubbing 을 이용하지 않는 케이스가 있으면 UnnecessaryStubbingException 이 발생된다.
            // 그럴때, lenient() 를 붙여주면 Test 실행시 stubbing 이용 여부를 체크하지 않는다.
            lenient().when(mockR2dbcEntityTemplate.select(eq(AuthEntity.class)))
                    .thenReturn(mockReactiveSelect);

            lenient().when(mockReactiveSelect.matching(any()))
                    .thenReturn(mockTerminatingSelect);
        }

        @DisplayName("토큰을 전달했는데 해당 토큰으로 결과를 찾을 수 없으면, Mono.empty 를 리턴한다.")
        @Test
        void when_auth_entity_is_empty_then_returns_empty_mono() {

            /**
             * 현재 이 Test 는 동작을 Test 한다기 보다 구현을 Test 하는 것에 더 가깝다.
             *
             * 예를 들면..
             * getNameByToken 에서 "token" 을 column 명으로 조건문을 작성하고 있는데
             * 해당 테스트에서 "token" 도 검증하고 있다.
             * 그러나, 사실 "token" 이 아니라 "token2" 로 바꾸어도 getNameByToken 동작이 변경되는 것은 아닌데
             * 해당 테스트는 Fail 이다. 즉, 구현을 검증하고 있다는 것이다.
             * (이는 private 메서드를 test 하는 느낌과 동일하다.)
             *
             * 테스트는 동작을 검증해야지 ... 구현을 검증하면 안된다.
             */

            // given
            // stubbing
            when(mockTerminatingSelect.one())
                    .thenReturn(Mono.empty());

            // when
            Mono<String> result = authService.getNameByToken(token);

            // then
            StepVerifier.create(result)
                    .expectComplete() // Mono empty 를 기대하므로 바로 complete 를 검증한다.
                    .verify();

            // ReactiveSelect 의 matching 이 실제로 실행되었는지 검증, 기본 값은 1회 실행 되었는지를 검증한다.
            verify(mockReactiveSelect).matching(queryArgumentCaptor.capture());

            // ReactiveSelect::matching 파라미터로 전달된 파라미터가 실제로 존재하는지 검증
            Query actualQuery = queryArgumentCaptor.getValue();
            assertTrue(actualQuery.getCriteria().isPresent());

            // token 이 getNameByToken 내에서 기대한대로 생성되어 ReactiveSelect::matching 파라미터로 전달되었는지 검증
            CriteriaDefinition criteriaDefinition = actualQuery.getCriteria().get();
            assertEquals(criteriaDefinition.getColumn().getReference(), "token");
            assertEquals(criteriaDefinition.getValue(), token);
        }

        @DisplayName("토큰을 전달했는데 해당 토큰으로 결과를 찾으면, Mono<String> 을 리턴한다.")
        @Test
        void when_auth_entity_is_not_empty_then_returns_mono_name() {

            // given
            Long userId = 100L;
            AuthEntity authEntity = new AuthEntity(1L, userId, token);

            // stubbing
            when(mockTerminatingSelect.one())
                    .thenReturn(Mono.just(authEntity));

            // when
            Mono<String> result = authService.getNameByToken(token);

            // then
            StepVerifier.create(result)
                    .expectNext(String.valueOf(userId))
                    .verifyComplete();
        }

        @DisplayName("토큰이 null 로 전달되면 Mono.error 반환")
        @Test
        void when_token_is_null_then_returns_mono_error() {

            // given
            token = null;

            // when
            Mono<String> result = authService.getNameByToken(token);

            // then
            StepVerifier.create(result)
                    .consumeErrorWith(
                            e -> {
                                assertInstanceOf(IllegalArgumentException.class, e); // error 는 IllegalArgumentException
                                assertEquals("token is invalid", e.getMessage());
                            }
                    ).verify();

            // 토큰이 null 이면, R2dbcEntityTemplate::select 는 실행되면 안된다.
            verify(mockR2dbcEntityTemplate, never()).select(any());
        }

        @DisplayName("토큰이 빈값으로 전달되면 Mono.error 반환")
        @Test
        void when_token_is_empty_string_then_returns_mono_error() {

            // given
            token = "";

            // when
            Mono<String> result = authService.getNameByToken(token);

            // then
            StepVerifier.create(result)
                    .consumeErrorWith(
                            e -> {
                                assertInstanceOf(IllegalArgumentException.class, e); // error 는 IllegalArgumentException
                                assertEquals("token is invalid", e.getMessage());
                            }
                    ).verify();

            // 토큰이 빈값 이면, R2dbcEntityTemplate::select 는 실행되면 안된다.
            verify(mockR2dbcEntityTemplate, never()).select(any());
        }

        @DisplayName("토큰 값이 admin 이면 admin 을 반환한다.")
        @Test
        void when_token_is_admin_then_returns_admin() {

            // given
            token = "admin";

            // when
            Mono<String> result = authService.getNameByToken(token);

            // then
            StepVerifier.create(result)
                    .expectNext("admin")
                    .verifyComplete();
        }
    }

}