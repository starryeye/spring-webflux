package dev.practice.user.service;

import dev.practice.user.client.ImageHttpClient;
import dev.practice.user.common.domain.EmptyImage;
import dev.practice.user.common.domain.Image;
import dev.practice.user.common.domain.User;
import dev.practice.user.common.repository.UserEntity;
import dev.practice.user.repository.UserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final AuthService authService;

    private final UserR2dbcRepository userR2dbcRepository;
    private final ImageHttpClient imageHttpClient;


    public Mono<User> findById(Long userId) {

        return userR2dbcRepository.findById(userId)
                .flatMap(
                        userEntity -> {

                            String imageId = userEntity.getProfileImageId();


                            return imageHttpClient.getImageResponseByImageId(imageId)
                                    .map(
                                            imageResponse -> new Image(
                                                    imageResponse.getId(),
                                                    imageResponse.getName(),
                                                    imageResponse.getUrl()
                                            )
                                    )
                                    .switchIfEmpty( // 응답 image 가 없다면
                                            Mono.just(new EmptyImage())
                                    ).map(
                                            image -> {

                                                Optional<Image> profileImage = Optional.empty();
                                                if (!(image instanceof EmptyImage)) {
                                                    // switchIfEmpty 를 타고 온게 아니라면 실제 값을 넣고,
                                                    // switchIfEmpty 를 타고 왔다면 if 문을 타지 않음 (Optional.empty())
                                                    profileImage = Optional.of(image);
                                                }

                                                return User.of(userEntity, profileImage);
                                            }
                                    );
                        }
                );
    }

    @Transactional
    public Mono<User> createUser(String name, Integer age, String password, String profileImageId) {

        // user 생성, DB insert
        // auth 생성, DB insert

        // profileImage 는 이미 만들어져 있다고 가정한다.
        // User 를 만들때 image 서버로 요청하여 image 를 가져와야하지만, 생략한다.

        UserEntity newUser = UserEntity.create(name, age, profileImageId, password);

        return userR2dbcRepository.save(newUser)
                .flatMap(
                        saved -> authService.createAuth(saved.getId())
                                .map(auth -> saved)
                )
                .map(
                        saved -> User.of(
                                saved,
                                Optional.of(new EmptyImage())
                        )
                );
    }
}
