package dev.practice.blocking.repository;

import dev.practice.common.repository.UserEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class UserRepository {
    private final Map<String, UserEntity> userMap;

    public UserRepository() {
        var user = new UserEntity("1234", "starryeye", 20, "image#1000");

        userMap = Map.of("1234", user);
    }

    @SneakyThrows
    public Optional<UserEntity> findById(String userId) {
        log.info("UserRepository.findById: {}", userId);

        Thread.sleep(1000); // 1초 지연

        // 주어진 userId 가 userMap 에 key 로 없으면 Optional empty 로 리턴
        var user = userMap.get(userId);
        return Optional.ofNullable(user);
    }
}