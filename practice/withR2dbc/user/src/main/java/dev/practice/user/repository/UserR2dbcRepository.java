package dev.practice.user.repository;

import dev.practice.user.common.repository.UserEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface UserR2dbcRepository extends R2dbcRepository<UserEntity, Long> {

    Flux<UserEntity> findAllByNameStartsWith(String prefix); // 사용되지는 않지만.. Test 예제를 위함
}
