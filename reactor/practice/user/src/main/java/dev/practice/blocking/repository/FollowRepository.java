package dev.practice.blocking.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class FollowRepository {
    private Map<String, Long> userFollowCountMap;

    public FollowRepository() {
        userFollowCountMap = Map.of("1234", 1000L);
    }

    @SneakyThrows
    public Long countByUserId(String userId) {
        log.info("FollowRepository.countByUserId: {}", userId);

        Thread.sleep(1000); // 1초 지연

        // 주어진 userId 를 가지는 userFollowCountMap value 가 없으면 기본 값 0 을 리턴한다.
        return userFollowCountMap.getOrDefault(userId, 0L);
    }
}