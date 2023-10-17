package dev.practice.future.repository;

import dev.practice.common.repository.ArticleEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ArticleFutureRepository {
    private static List<ArticleEntity> articleEntities;

    public ArticleFutureRepository() {
        articleEntities = List.of(
                new ArticleEntity("1", "소식1", "내용1", "1234"),
                new ArticleEntity("2", "소식2", "내용2", "1234"),
                new ArticleEntity("3", "소식3", "내용3", "10000")
        );
    }

    @SneakyThrows
    public CompletableFuture<List<ArticleEntity>> findAllByUserId(String userId) {

        // CompletableFuture 를 반환한다. ForkJoinPool 에서 스레드를 할당하여 supplyAsync 를 Caller 관점에서 non blocking 으로 수행한다.
        return CompletableFuture.supplyAsync(() -> {
            log.info("ArticleRepository.findAllByUserId: {}", userId);

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return articleEntities.stream()
                    .filter(articleEntity -> articleEntity.getUserId().equals(userId))
                    .toList();
        });
    }
}