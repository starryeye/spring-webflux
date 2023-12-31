package dev.practice.reactor.repository;

import dev.practice.common.repository.UserEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class FollowReactorRepository {
    private Map<String, Long> userFollowCountMap;

    public FollowReactorRepository() {
        userFollowCountMap = Map.of("1234", 1000L);
    }

    @SneakyThrows
    public Mono<Long> countByUserId(String userId) {

        // Mono 에서 sequence 를 만드는 create(Consumer<MonoSink<T>> callback) 사용, 비동기로 Mono 를 생성한다.
        return Mono.create(sink -> {
            log.info("FollowRepository.countByUserId: {}, tx: {}", userId, Thread.currentThread().getName());

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // defaultValue 가 존재하므로 값이 없을 수 없으므로 값이 있는 경우만 생각해주면 된다.
            sink.success(userFollowCountMap.getOrDefault(userId, 0L));
        });
    }

    public Mono<Long> countWithContext() {

        // deferContextual 을 사용하여 contextView 를 받고 publisher 를 반환하는 Function 을 쓴다.
        return Mono.deferContextual(contextView -> {

                    Optional<UserEntity> userEntityOptional = contextView.getOrEmpty("user");

                    if (userEntityOptional.isEmpty()) {
                        throw new RuntimeException("user not found"); // Article 에서와 마찬가지로.. 필요 없을 듯..
                    }

                    return Mono.just(userEntityOptional.get().getId());
                })
                .flatMap(this::countByUserId);
    }
}
