package dev.practice.user.common.domain;

import dev.practice.user.common.repository.AuthEntity;
import lombok.Data;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class Auth {

    private static final Integer TOKEN_LENGTH = 6;
    private static final String TOKEN_INGREDIENT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final Random random = new Random();

    private final Long userId;
    private final String token;

    public static Auth of(AuthEntity authEntity) {
        return new Auth(
                authEntity.getUserId(),
                authEntity.getToken()
        );
    }

    public static String createToken() {

        return IntStream.range(0, TOKEN_LENGTH)
                .map(i -> random.nextInt(TOKEN_INGREDIENT.length()))
                .mapToObj(TOKEN_INGREDIENT::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
