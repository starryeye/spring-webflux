package dev.practice.future;

import dev.practice.common.domain.Article;
import dev.practice.common.domain.Image;
import dev.practice.common.domain.User;
import dev.practice.common.repository.UserEntity;
import dev.practice.future.repository.ArticleFutureRepository;
import dev.practice.future.repository.FollowFutureRepository;
import dev.practice.future.repository.ImageFutureRepository;
import dev.practice.future.repository.UserFutureRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UserFutureService {
    private final UserFutureRepository userRepository;
    private final ArticleFutureRepository articleRepository;
    private final ImageFutureRepository imageRepository;
    private final FollowFutureRepository followRepository;

    @SneakyThrows
    public CompletableFuture<Optional<User>> getUserById(String id) {

        // CompletableFuture 를 리턴
        // public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn)
        // Java stream 의 flatMap 과 비슷하다.
        // T: findById 의 리턴 값(Optional<UserEntity>)을 이용 하면서,
        // CompletionStage<U>: 또다른 CompletableFuture(CompletableFuture<Optional<User>>) 를 적용할 것이다. (내부에서 또다른 작업을 함)
        return userRepository.findById(id)
                .thenComposeAsync(this::getUser); // *Async 를 이용(Caller, Callee 할당 스레드가 아닌 PorkJoinPool 에서 새로 할당)
    }

    @SneakyThrows
    private CompletableFuture<Optional<User>> getUser(Optional<UserEntity> userEntityOptional) {

        if (userEntityOptional.isEmpty()) { // 이전 CompletableFuture 의 결과가 없으면, 이후 작업은 할 필요 없다.
            return CompletableFuture.completedFuture(Optional.empty());
        }

        var userEntity = userEntityOptional.get(); // 이전 CompletableFuture 의 결과를 할당.


        // UserBlockingService 에서는 1초간 blocking 되었지만..
        // 여기서는 CompletableFuture 를 반환 받았다. ForkJoinPool 에서 findById 내부 작업을 수행한다. (non blocking)
        // 결과의 관심(동기, imageEntityOptional)은 thenApplyAsync 로 callback 형태로 전달하였고
        // *Async 이므로 새로운 ForkJoinPool 에서 할당 받아 작업된다.
        var imageFuture = imageRepository.findById(userEntity.getProfileImageId())
                .thenApplyAsync(imageEntityOptional ->
                        imageEntityOptional.map(imageEntity ->
                                new Image(imageEntity.getId(), imageEntity.getName(), imageEntity.getUrl())
                        )
                );

        // imageFuture 와 동일
        var articlesFuture = articleRepository.findAllByUserId(userEntity.getId())
                .thenApplyAsync(articleEntities ->
                        articleEntities.stream()
                                .map(articleEntity ->
                                        new Article(articleEntity.getId(), articleEntity.getTitle(), articleEntity.getContent())
                                )
                                .collect(Collectors.toList())
                );

        // countByUserId 의 결과로는 따로 하는게 없으므로 callback 작업(위의 thenApplyAsync) 은 할 필요 없음
        var followCountFuture = followRepository.countByUserId(userEntity.getId());

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