package dev.practice.reactor;

import dev.practice.common.domain.Article;
import dev.practice.common.domain.EmptyImage;
import dev.practice.common.domain.Image;
import dev.practice.common.domain.User;
import dev.practice.common.repository.ImageEntity;
import dev.practice.common.repository.UserEntity;
import dev.practice.reactor.repository.ArticleReactorRepository;
import dev.practice.reactor.repository.FollowReactorRepository;
import dev.practice.reactor.repository.ImageReactorRepository;
import dev.practice.reactor.repository.UserReactorRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UserReactorService {
    private final UserReactorRepository userRepository;
    private final ArticleReactorRepository articleRepository;
    private final ImageReactorRepository imageRepository;
    private final FollowReactorRepository followRepository;

    @SneakyThrows
    public Mono<User> getUserById(String id) {

        // Mono 를 리턴
        // CompletableFuture 에서는 빈 값을 표현할 수 없어서 T 를 Optional<User> 로 사용했었다..
        // Mono 를 사용함으로써 값이 있거나 없거나로 표현할 수 있다.

        return userRepository.findById(id)
                // findById 에서 값이 없는 Mono.empty 라면 flatMap 포함 이후의 연산자는 동작하지 않는다.
                // 즉 아래의 flatMap 에서 userEntity 는 not null 이다.
                .flatMap(userEntity -> Mono.fromFuture(this.getUser(userEntity)))
                .map(Optional::get);
    }

    @SneakyThrows
    private CompletableFuture<Optional<User>> getUser(UserEntity userEntity) {


        // map 연산자에 도달한 Mono 는 값이 있음이 보장된다. 값이 없다면 map 포함 이후의 연산자가 동작하지 않음
        // 하지만, findById 내부에서 값이 없다면 예외가 발생한다. -> onErrorReturn 으로 기본 값으로 하여 onComplete 를 발생시킨다. (Flux 라면.. 이후 값들이 있어도 흐르지 않음)
        Mono<Image> imageMono = imageRepository.findById(userEntity.getProfileImageId())
                .map(imageEntity -> new Image(imageEntity.getId(), imageEntity.getName(), imageEntity.getUrl()))
                .onErrorReturn(new EmptyImage());


        // collectList 로 findAllByUserId 에서 onComplete 이벤트가 발생하기 전까지 모든 element 를 내부 리스트에 적재하다가..
        // onComplete 이벤트가 발생하면 리스트를 하나의 값으로 한 Mono 를 onComplete 시킨다.
        Mono<List<Article>> articlesMono = articleRepository.findAllByUserId(userEntity.getId())
                .map(articleEntity -> new Article(articleEntity.getId(), articleEntity.getTitle(), articleEntity.getContent()))
                .collectList();

        
        Mono<Long> followCountMono = followRepository.countByUserId(userEntity.getId());

        // allOf 를 사용하여 파라미터로 전달된 CompletableFuture 들의 모든 작업이 종료 시점을 알 수 있도록 한다.
        return CompletableFuture.allOf(imageFuture, articlesFuture, followCountFuture)
                .thenApplyAsync(v -> { // 모든 작업이 종료 되면 아래 작업을 수행한다. (*Async 를 사용)
                    try {
                        var image = imageFuture.get();
                        var articles = articlesFuture.get();
                        var followCount = followCountFuture.get();

                        return Optional.of(
                                new User(
                                        userEntity.getId(),
                                        userEntity.getName(),
                                        userEntity.getAge(),
                                        image,
                                        articles,
                                        followCount
                                )
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // 결과적으로 getUser 메서드 내부에서는...
        // 3개의 repository 를 access 하여 바로 CompletableFuture 를 리턴 받아서
        // 기존에 최소 1+3초 이상 걸리던 동기 blocking 로직을.. 최소 1+1초 걸리도록 개선하였다.
        // 사용되는 스레드는 정말 많은데.. repository 에 access 될 때마다 전부 다른 스레드가 사용되고
        // callback 작업들(*Async)도 모두 다른 스레드가 사용된다.
    }
}