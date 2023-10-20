package dev.practice.reactor;

import dev.practice.common.domain.User;
import dev.practice.future.UserFutureService;
import dev.practice.future.repository.ArticleFutureRepository;
import dev.practice.future.repository.FollowFutureRepository;
import dev.practice.future.repository.ImageFutureRepository;
import dev.practice.future.repository.UserFutureRepository;
import dev.practice.reactor.repository.ArticleReactorRepository;
import dev.practice.reactor.repository.FollowReactorRepository;
import dev.practice.reactor.repository.ImageReactorRepository;
import dev.practice.reactor.repository.UserReactorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserReactorServiceTest {

    private UserReactorService userReactorService;
    private UserReactorRepository userRepository;
    private ArticleReactorRepository articleRepository;
    private ImageReactorRepository imageRepository;
    private FollowReactorRepository followRepository;


    @BeforeEach
    void setUp() {
        userRepository = new UserReactorRepository();
        articleRepository = new ArticleReactorRepository();
        imageRepository = new ImageReactorRepository();
        followRepository = new FollowReactorRepository();

        userReactorService = new UserReactorService(
                userRepository, articleRepository, imageRepository, followRepository
        );
    }

    @Test
    void getUserEmptyIfInvalidUserIdIsGiven() throws ExecutionException, InterruptedException {
        // given
        String userId = "invalid_user_id";

        // when
        // Mono::blockingOptional Mono 에 blocking 옵션을 주고.. 결과를 Optional 로 반환 받음
        Optional<User> user = userReactorService.getUserById(userId).blockOptional();

        // then
        assertTrue(user.isEmpty());
    }

    @Test
    void testGetUser() throws ExecutionException, InterruptedException {
        // given
        String userId = "1234";

        // when
        long beforeTime = System.currentTimeMillis();

        Optional<User> optionalUser = userReactorService.getUserById(userId).blockOptional();

        long afterTime = System.currentTimeMillis();
        long diffTime = afterTime - beforeTime;
        System.out.println("실행 시간(ms): " + diffTime / 1000);

        // then
        assertFalse(optionalUser.isEmpty());
        var user = optionalUser.get();
        assertEquals(user.getName(), "starryeye");
        assertEquals(user.getAge(), 20);

        assertFalse(user.getProfileImage().isEmpty());
        var image = user.getProfileImage().get();
        assertEquals(image.getId(), "image#1000");
        assertEquals(image.getName(), "profileImage");
        assertEquals(image.getUrl(), "https://practice.dev/images/1000");

        assertEquals(2, user.getArticleList().size());

        assertEquals(1000, user.getFollowCount());
    }
}
