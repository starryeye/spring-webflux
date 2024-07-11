package dev.starryeye.initialization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

import static org.assertj.core.api.Assertions.assertThat;

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


}