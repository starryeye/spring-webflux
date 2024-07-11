package dev.starryeye.initialization;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ContextConfiguration(classes = R2dbcAuditingTestConfig.class)
@DataR2dbcTest
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @AfterEach
    void tearDown() {
        r2dbcEntityTemplate.delete(Article.class)
                .all()
                .block();
    }

    @Test
    void smoke_test() {
        assertThat(articleRepository).isNotNull();
        assertThat(r2dbcEntityTemplate).isNotNull();
    }

    @Test
    void save() {

        // given
        String title = "title";
        String author = "author";
        String content = "content";
        Article created = Article.create(title, author, content);

        // when
        Mono<Article> saved = articleRepository.save(created);

        // then
        StepVerifier.create(saved)
                .assertNext(article -> {
                    assertThat(article.getTitle()).isEqualTo(title);
                    assertThat(article.getAuthor()).isEqualTo(author);
                    assertThat(article.getContent()).isEqualTo(content);

                    assertThat(article.getId()).isNotNull();
                    assertThat(article.getCreatedAt()).isNotNull();
                    assertThat(article.getUpdatedAt()).isNotNull();

                    log.info("id: {}, createdAt: {}, updatedAt: {}", article.getId(), article.getCreatedAt(), article.getUpdatedAt());
                })
                .verifyComplete();
    }
}