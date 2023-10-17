package dev.practice.future.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class FollowFutureRepository {
    private Map<String, Long> userFollowCountMap;

    public FollowFutureRepository() {
        userFollowCountMap = Map.of("1234", 1000L);
    }

    @SneakyThrows
    public CompletableFuture<Long> countByUserId(String userId) {

        // CompletableFuture 를 반환한다. ForkJoinPool 에서 스레드를 할당하여 supplyAsync 를 Caller 관점에서 non blocking 으로 수행한다.
        return CompletableFuture.supplyAsync(() -> {
            log.info("FollowRepository.countByUserId: {}", userId);

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return userFollowCountMap.getOrDefault(userId, 0L);
        });
    }
}
