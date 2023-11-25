package dev.practice.user.common.repository;

import lombok.Data;

@Data
public class UserEntity {
    private final Long id;
    private final String name;
    private final Integer age;
    private final String profileImageId;
    private final String password;
}
