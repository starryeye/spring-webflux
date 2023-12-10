package dev.practice.user.service;

import dev.practice.user.common.domain.Auth;
import dev.practice.user.common.repository.AuthEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthService {

    /**
     * 헤더로 들어온 인증 토큰(X-I-AM)이 유효한지 확인한 후..
     * 유효하다면 토큰에 해당하는 특정 값(userId)을 반환한다.
     */

    // Repository 를 사용하지 않고 R2dbcEntityTemplate 을 사용해본다.
    private final R2dbcEntityTemplate entityTemplate;

    public Mono<String> getNameByToken(String token) {

        if(Objects.isNull(token) || token.isBlank()) {
            return Mono.error(new IllegalArgumentException("token is invalid"));
        }

        if(token.equals("admin")) {
            return Mono.just("admin");
        }

        Query query = Query.query(
                Criteria.where("token").is(token)
        );

        //entityTemplate.selectOne 을 사용해도 된다.
        return entityTemplate.select(AuthEntity.class)
                .matching(query)
                .one() // 실제 쿼리를 보면, limit 2 로 실행하는데.. 이는 결과가 2 이면 예외를 발생시키기 위함이다.
                .map(
                        authEntity -> authEntity.getUserId().toString()
                ).doOnNext(
                        name -> log.info("Auth R2dbcEntityTemplate select, userId={}", name)
                );
    }

    public Mono<Auth> createAuth(Long userId) {

        String newToken = Auth.createToken();

        AuthEntity newAuth = AuthEntity.create(userId, newToken);

        return entityTemplate.insert(newAuth)
                .map(Auth::of);
    }
}
