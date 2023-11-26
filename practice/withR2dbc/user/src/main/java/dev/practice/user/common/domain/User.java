package dev.practice.user.common.domain;

import dev.practice.user.common.repository.UserEntity;
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

    public static User of(UserEntity entity, Optional<Image> profileImage) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getAge(),
                profileImage,
                List.of(),
                0L
        );
    }
}
