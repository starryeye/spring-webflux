package dev.practice.user.repository;

import dev.practice.user.common.repository.UserEntity;

public class TestDataBuilderForRepository {

    public static UserEntity createUnsavedUserEntity(
            String name
    ) {
        return UserEntity.create(name, 20, "1", "123123");
    }
}
