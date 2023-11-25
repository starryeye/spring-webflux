package dev.practice.user.service;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AuthService {

    /**
     * 헤더로 들어온 인증 토큰(X-I-AM)이 유효한지 확인한 후..
     * 유효하다면 토큰에 해당하는 특정 값(userId)을 반환한다.
     */

    private static final Map<String, String> tokenUserIdMap = Map.of("abcd", "1234");

    public Mono<String> getNameByToken(String token) {
        return Mono.justOrEmpty(tokenUserIdMap.get(token));
    }
}
