package dev.practice.future.repository;

import dev.practice.common.repository.UserEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class UserFutureRepository {
    private final Map<String, UserEntity> userMap;

    public UserFutureRepository() {
        var user = new UserEntity("1234", "starryeye", 20, "image#1000");

        userMap = Map.of("1234", user);
    }

    @SneakyThrows
    public CompletableFuture<Optional<UserEntity>> findById(String userId) {

        // CompletableFuture 를 반환한다. ForkJoinPool 에서 스레드를 할당하여 supplyAsync 를 Caller 관점에서 non blocking 으로 수행한다.
        return CompletableFuture.supplyAsync(() -> {
            log.info("UserRepository.findById: {}", userId);

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            var user = userMap.get(userId);
            return Optional.ofNullable(user);
        });
    }
}