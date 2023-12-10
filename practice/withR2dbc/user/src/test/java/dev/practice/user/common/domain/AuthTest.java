package dev.practice.user.common.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthTest {

    /**
     * Unit Test (Domain test, 비즈니스 테스트에 적합)
     */

    private Integer TOKEN_LENGTH = 6;
    private String TOKEN_INGREDIENT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    @Test
    void when_call_then_return_random_token() {

        // given
        // when
        String result = Auth.createToken();

        // then
        assertLinesMatch(List.of("^[A-Za-z]{6}$"), List.of(result)); //메서드가 6자리의 알파벳 문자(대문자 또는 소문자)로 구성된 문자열을 반환하는지 확인
    }


    @Test
    void testCreateToken_Length() {

        // given
        // when
        String result = Auth.createToken();

        // then
        assertEquals(TOKEN_LENGTH, result.length(), "토큰 길이는 6 이어야 합니다.");
    }

    @Test
    void testCreateToken_Composition() {

        // given
        // when
        String result = Auth.createToken();

        // then
        for (char c : result.toCharArray()) {
            assertTrue(TOKEN_INGREDIENT.indexOf(c) >= 0, "토큰에 유효하지 않은 문자가 포함되어 있습니다.");
        }
    }
}