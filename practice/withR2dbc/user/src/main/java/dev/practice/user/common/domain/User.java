package dev.practice.user.common.domain;

import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class User {
    private final Long id;
    private final String name;
    private final Integer age;
    private final Optional<Image> profileImage;
    private final List<Article> articleList;
    private final Long followCount;
}
