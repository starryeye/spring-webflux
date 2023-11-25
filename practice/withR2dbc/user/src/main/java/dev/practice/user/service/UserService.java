package dev.practice.user.service;

import dev.practice.user.common.domain.EmptyImage;
import dev.practice.user.common.domain.Image;
import dev.practice.user.common.domain.User;
import dev.practice.user.repository.UserReactorRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserReactorRepository userReactorRepository;

    // image server 로 image 정보 요청
    private final WebClient webClient;

    public UserService(UserReactorRepository userReactorRepository) {
        this.userReactorRepository = userReactorRepository;
        this.webClient = WebClient.create("http://localhost:8081");
    }

    public Mono<User> findById(Long userId) {

        return userReactorRepository.findById(userId)
                .flatMap(
                        userEntity -> {

                            String imageId = userEntity.getProfileImageId();
                            Map<String, String> urlVariableMap = Map.of("imageId", imageId);

                            return webClient.get()
                                    .uri("/api/images/{imageId}", urlVariableMap)
                                    .retrieve() // 요청을 서버에 전달, ResponseSpec 을 반환
                                    .toEntity(ImageResponse.class) // Mono<ResponseEntity<ImageResponse>>
                                    .map(
                                            responseEntity -> responseEntity.getBody()
                                    )
                                    .map(
                                            body -> new Image(
                                                    body.getId(),
                                                    body.getName(),
                                                    body.getUrl()
                                            )
                                    )
                                    .switchIfEmpty( // 응답 image 가 없다면
                                            Mono.just(new EmptyImage())
                                    )
                                    .map(
                                            image -> {

                                                Optional<Image> profileImage = Optional.empty();
                                                if (!(image instanceof EmptyImage)) {
                                                    // switchIfEmpty 를 타고 온게 아니라면 실제 값을 넣고,
                                                    // switchIfEmpty 를 타고 왔다면 if 문을 타지 않음 (Optional.empty())
                                                    profileImage = Optional.of(image);
                                                }

                                                return new User(
                                                        userEntity.getId(),
                                                        userEntity.getName(),
                                                        userEntity.getAge(),
                                                        profileImage,
                                                        List.of(),
                                                        0L
                                                );
                                            }
                                    );
                        }
                );
    }
}
