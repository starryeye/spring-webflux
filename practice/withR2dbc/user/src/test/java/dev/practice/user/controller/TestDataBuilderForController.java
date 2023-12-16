package dev.practice.user.controller;

import dev.practice.user.common.domain.Image;
import dev.practice.user.common.domain.User;

import java.util.Collections;
import java.util.Optional;

public class TestDataBuilderForController {

    public static User createUser(Long id) {
        var profileImage = new Image(
                "1",
                "tester's profile",
                "https://practice.dev/images/1"
        );

        return new User(
                id,
                "tester",
                20,
                Optional.of(profileImage),
                Collections.emptyList(),
                100L
        );
    }

    public static User createUser(
            Long id,
            String name,
            Integer age,
            String password,
            String profileImageId
    ) {
        var profileImage = new Image(
                profileImageId,
                "tester's profile",
                "https://practice.dev/images/1"
        );

        return new User(
                id,
                name,
                age,
                Optional.of(profileImage),
                Collections.emptyList(),
                100L
        );
    }
}
