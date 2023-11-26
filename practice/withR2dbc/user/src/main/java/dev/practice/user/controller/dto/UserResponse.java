package dev.practice.user.controller.dto;

import dev.practice.user.common.domain.User;
import lombok.Data;

import java.util.Optional;

@Data
public class UserResponse {
    private final Long id;
    private final String name;
    private final Integer age;
    private final Long followCount;
    private final Optional<ProfileImageResponse> imageResponse;

    public static UserResponse of(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getAge(),
                user.getFollowCount(),
                user.getProfileImage()
                        .map(
                                image -> new ProfileImageResponse(
                                        image.getId(),
                                        image.getName(),
                                        image.getUrl()
                                )
                        )
        );
    }
}
