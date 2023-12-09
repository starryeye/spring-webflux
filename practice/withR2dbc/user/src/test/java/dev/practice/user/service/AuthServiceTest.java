package dev.practice.user.service;

import dev.practice.user.common.repository.AuthEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;

import static org.junit.jupiter.api.Assertions.*;

// ExtendWith 로 MockitoExtension 을 하여, @Mock, @InjectMocks 를 사용할 수 있도록 함
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    R2dbcEntityTemplate mockR2dbcEntityTemplate; // prefix 로 mock 을 붙여 Mock 객체임을 강조한다.

    // InjectMocks 를 사용하여 AuthService 를 생성하는데 필요한 의존성 객체(R2dbcEntityTemplate) 가 있을 경우 Mock 으로 집어 넣는다.
    // 위 mockR2dbcEntityTemplate 와 동일한 객체가 주입된다.
    @InjectMocks
    AuthService authService;


    @Test
    void authServiceNotNull() {
        assertNotNull(authService); // 잘 생성 되었는지 확인
        assertTrue(MockUtil.isMock(mockR2dbcEntityTemplate)); // Mock 객체인지 확인
    }

    @Nested // Nested 를 통하여, getNameByToken 메서드 테스트하는 공간을 class 로 분리한다.
    class GetNameByToken {

    }

}