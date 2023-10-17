package dev.practice.blocking;

import dev.practice.blocking.repository.ArticleRepository;
import dev.practice.blocking.repository.FollowRepository;
import dev.practice.blocking.repository.ImageRepository;
import dev.practice.blocking.repository.UserRepository;
import dev.practice.common.domain.Article;
import dev.practice.common.domain.Image;
import dev.practice.common.domain.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class UserBlockingService {

    private final ArticleRepository articleRepository;
    private final FollowRepository followRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    public Optional<User> getUserById(String id) {

        // 총 4초, 주어진 id 로 repository 에 접근하여 정보를 빼오고 user 를 리턴한다.
        return userRepository.findById(id) // 1초
                .map(user -> {

                    // 1초
                    var image = imageRepository.findById(user.getProfileImageId())
                            .map( // Entity 를 뽑아서 도메인 객체로 만든다.
                                    imageEntity -> new Image(imageEntity.getId(), imageEntity.getName(), imageEntity.getUrl())
                            );

                    // 1초
                    var articles = articleRepository.findAllByUserId(user.getId()).stream()
                            .map( // Entity 를 뽑아서 도메인 객체로 만든다.
                                    articleEntity -> new Article(articleEntity.getId(), articleEntity.getTitle(), articleEntity.getContent())
                            ).toList();

                    // 1초
                    var followCount = followRepository.countByUserId(user.getId());

                    return new User( // 도메인 객체로 만든다.
                            user.getId(),
                            user.getName(),
                            user.getAge(),
                            image,
                            articles,
                            followCount
                    );
                });
    }
}
