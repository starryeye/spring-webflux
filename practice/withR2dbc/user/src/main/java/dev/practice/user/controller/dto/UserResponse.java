package dev.practice.user.controller.dto;

import lombok.Data;

import java.util.Optional;

@Data
public class UserResponse {
    private final Long id;
    private final String name;
    private final Integer age;
    private final Long followCount;
    private final Optional<ProfileImageResponse> imageResponse;
}
