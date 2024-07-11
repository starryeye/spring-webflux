package dev.starryeye.initialization;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ArticleRepository extends R2dbcRepository<Article, Long> {
}
