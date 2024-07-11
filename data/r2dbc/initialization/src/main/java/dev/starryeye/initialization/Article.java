package dev.starryeye.initialization;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Table("ARTICLES")
public class Article extends BaseEntity{

    @Id
    private final Long id;

    private final String title;
    private final String author;
    private final String content;

    @Builder
    private Article(LocalDateTime createdAt, LocalDateTime updatedAt, String author, String content, String title, Long id) {
        super(createdAt, updatedAt);
        this.author = author;
        this.content = content;
        this.title = title;
        this.id = id;
    }

    public static Article create(String title, String author, String content) {
        return Article.builder()
                .createdAt(null)
                .updatedAt(null)
                .id(null)
                .title(title)
                .author(author)
                .content(content)
                .build();
    }

    public Article changeContent(String content) {
        return Article.builder()
                .createdAt(getCreatedAt())
                .updatedAt(null)
                .id(getId())
                .title(getTitle())
                .author(getAuthor())
                .content(content)
                .build();
    }
}
