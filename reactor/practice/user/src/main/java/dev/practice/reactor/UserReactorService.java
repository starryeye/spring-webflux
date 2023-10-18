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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public class UserReactorService {
    private final UserReactorRepository userRepository;
    private final ArticleReactorRepository articleRepository;
    private final ImageReactorRepository imageRepository;
    private final FollowReactorRepository followRepository;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @SneakyThrows
    public Mono<User> getUserById(String id) {

        // Mono 를 리턴
        // CompletableFuture 에서는 빈 값을 표현할 수 없어서 T 를 Optional<User> 로 사용했었다..
        // Mono 를 사용함으로써 값이 있거나 없거나로 표현할 수 있다.

        return userRepository.findById(id)
                // findById 에서 값이 없는 Mono.empty 라면 flatMap 포함 이후의 연산자는 동작하지 않는다.
                // 즉 아래의 flatMap 에서 userEntity 는 not null 이다.
                .flatMap(this::getUser);
    }

    @SneakyThrows
    private Mono<User> getUser(UserEntity userEntity) {

        // context 생성, k: "user", v: userEntity 객체 (findById 결과)
        Context context = Context.of("user", userEntity);


        // map 연산자에 도달한 Mono 는 값이 있음이 보장된다. 값이 없다면 map 포함 이후의 연산자가 동작하지 않음
        // 하지만, findById 내부에서 값이 없다면 예외가 발생한다. -> onErrorReturn 으로 기본 값으로 하여 onComplete 를 발생시킨다. (Flux 라면.. 이후 값들이 있어도 흐르지 않음)
        Mono<Image> imageMono = imageRepository.findWithContext()
                .map(imageEntity -> new Image(imageEntity.getId(), imageEntity.getName(), imageEntity.getUrl()))
                .onErrorReturn(new EmptyImage())
                .subscribeOn(Schedulers.fromExecutorService(executorService))
                .contextWrite(context); // 해당 파이프라인 "위"로 context 적용

        log.info("UserService1 tx: {}", Thread.currentThread().getName());


        // collectList 로 findAllByUserId 에서 onComplete 이벤트가 발생하기 전까지 모든 element 를 내부 리스트에 적재하다가..
        // onComplete 이벤트가 발생하면 리스트를 하나의 값으로 한 Mono 를 onComplete 시킨다. (Flux -> Mono)
        Mono<List<Article>> articlesMono = articleRepository.findAllWithContext()
                .skip(5) // 최초 5개 스킵
                .take(2) // 최초 두개만 취한다 -> 위와 합쳐서 6, 7 번째를 취함
                .map(articleEntity -> new Article(articleEntity.getId(), articleEntity.getTitle(), articleEntity.getContent()))
                .collectList()
                .subscribeOn(Schedulers.fromExecutorService(executorService))
                .contextWrite(context); // 해당 파이프라인 "위"로 context 적용

        log.info("UserService2 tx: {}", Thread.currentThread().getName());

        // countByUserId 에서 리턴한 Mono<Long> 의 값(Long) 을 그대로 사용하므로 추가 연산하지 않는다.
        Mono<Long> followCountMono = followRepository.countWithContext()
                .subscribeOn(Schedulers.fromExecutorService(executorService))
                .contextWrite(context);

        log.info("UserService3 tx: {}", Thread.currentThread().getName());

        // concat, merge, mergeSequencial, zip 사용에 따른 동작 방식과 특성 생각해보기
        return Mono.zip(imageMono, articlesMono, followCountMono) // 여러 publisher 를 합친다.(각 publisher 에서 1개가 준비되면 tuple 로 묶어서 하나씩 방출)
                .map(resultTuple -> {
                    Image image = resultTuple.getT1();
                    List<Article> articles = resultTuple.getT2();
                    Long followCount = resultTuple.getT3();

                    Optional<Image> imageOptional = Optional.empty();
                    if (!(image instanceof EmptyImage)) {
                        imageOptional = Optional.of(image);
                    }

                    log.info("UserService zip tx: {}", Thread.currentThread().getName()); //TODO 이게 Test worker 로 나오게 하려면?

                    return new User(
                            userEntity.getId(),
                            userEntity.getName(),
                            userEntity.getAge(),
                            imageOptional,
                            articles,
                            followCount
                    );
                });
    }
}