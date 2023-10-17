package dev.practice.blocking.repository;

import dev.practice.common.repository.ArticleEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ArticleRepository {
    private static List<ArticleEntity> articleEntities;

    public ArticleRepository() {
        articleEntities = List.of(
                new ArticleEntity("1", "소식1", "내용1", "1234"),
                new ArticleEntity("2", "소식2", "내용2", "1234"),
                new ArticleEntity("3", "소식3", "내용3", "10000")
        );
    }

    @SneakyThrows
    public List<ArticleEntity> findAllByUserId(String userId) {
        log.info("ArticleRepository.findAllByUserId: {}", userId);

        Thread.sleep(1000); // 1초 지연

        // 주어진 userId 를 가지는 ArticleEntity 를 List 로 리턴
        return articleEntities.stream()
                .filter(articleEntity -> articleEntity.getUserId().equals(userId))
                .toList();
    }
}