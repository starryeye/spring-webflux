package dev.practice.blocking;

import dev.practice.blocking.repository.ArticleRepository;
import dev.practice.blocking.repository.FollowRepository;
import dev.practice.blocking.repository.ImageRepository;
import dev.practice.blocking.repository.UserRepository;
import dev.practice.common.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserBlockingServiceTest {
    UserBlockingService userBlockingService;
    UserRepository userRepository;
    ArticleRepository articleRepository;
    ImageRepository imageRepository;
    FollowRepository followRepository;

    @BeforeEach
    void setUp() {

        articleRepository = new ArticleRepository();
        followRepository = new FollowRepository();
        imageRepository = new ImageRepository();
        userRepository = new UserRepository();

        userBlockingService = new UserBlockingService(
                articleRepository, followRepository, imageRepository, userRepository
        );
    }

    @Test
    void getUserEmptyIfInvalidUserIdIsGiven() {
        // given
        String userId = "invalid_user_id";

        // when
        Optional<User> user = userBlockingService.getUserById(userId);

        // then
        assertTrue(user.isEmpty());
    }

    @Test
    void testGetUser() {
        // given
        String userId = "1234";

        // when
        long beforeTime = System.currentTimeMillis();

        Optional<User> optionalUser = userBlockingService.getUserById(userId);

        long afterTime = System.currentTimeMillis();
        long diffTime = afterTime - beforeTime;
        System.out.println("실행 시간(ms): " + diffTime / 1000);

        // then
        assertFalse(optionalUser.isEmpty());
        var user = optionalUser.get();
        assertEquals(user.getName(), "taewoo");
        assertEquals(user.getAge(), 32);

        assertFalse(user.getProfileImage().isEmpty());
        var image = user.getProfileImage().get();
        assertEquals(image.getId(), "image#1000");
        assertEquals(image.getName(), "profileImage");
        assertEquals(image.getUrl(), "https://dailyone.com/images/1000");

        assertEquals(2, user.getArticleList().size());

        assertEquals(1000, user.getFollowCount());
    }
}