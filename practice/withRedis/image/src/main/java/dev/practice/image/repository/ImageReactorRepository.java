package dev.practice.image.repository;

import dev.practice.image.common.repository.ImageEntity;
import dev.practice.image.common.repository.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
public class ImageReactorRepository {

    /**
     * spring-data-redis-reactive 에서는 repository interface 를 제공하지 않는다.
     * 따라서, ReactiveRedisTemplate 이 사용자가 사용할 수 있는 가장 고수준의 객체이다.
     * 하지만, 여기서는 ReactiveRedisTemplate 을 직접 사용하지 않고 ReactiveHashOperations 을 사용할 것임.
     *
     * 참고
     * spring-data-redis 에서는 hash 기반의 repository interface 를 제공한다.
     */
    private final ReactiveHashOperations<String, String, String> hashOperations;

    public ImageReactorRepository(
            ReactiveStringRedisTemplate reactiveStringRedisTemplate
    ) {
        this.hashOperations = reactiveStringRedisTemplate.opsForHash();

//        imageMap = Map.of(
//                "1", new ImageEntity("1", "starryeye's profileImage", "https://practice.dev/images/1"),
//                "2", new ImageEntity("2", "Alice's profileImage", "https://practice.dev/images/2")
//        );
    }

    @SneakyThrows
    public Mono<ImageEntity> findById(String id) {

        // Mono 에서 sequence 를 만드는 create(Consumer<MonoSink<T>> callback) 사용, 비동기로 Mono 를 생성한다.
        // create 는 Thread.sleep 을 수행하지 않고 non-blocking 으로 넘어가기 위함이다. (findById 메서드 호출 스레드입장)
        return Mono.create(sink -> {
            log.info("ImageRepository.findById: {}, tx: {}", id, Thread.currentThread().getName());

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            hashOperations.multiGet(id, List.of("id", "name", "url"))
                    .doOnNext(
                            strings -> log.info("strings={}", strings)
                    )
                    .subscribe( // todo, subscribe 를 쓰지말고 doOnNext 을 쓰면? 스레드 관점에서 생각해보기
                            strings -> {

                                if(strings.stream().allMatch(Objects::isNull)) {
                                    sink.error(new RuntimeException("image not found"));
                                    return;
                                }

                                ImageEntity image = new ImageEntity(
                                        strings.get(0),
                                        strings.get(1),
                                        strings.get(2)
                                );

                                sink.success(image);
                            }
                    );
        });
    }

    public Mono<ImageEntity> findWithContext() {

        // deferContextual 을 사용하여 contextView 를 받고 publisher 를 반환하는 Function 을 쓴다.
        return Mono.deferContextual(contextView -> {

                    Optional<UserEntity> userEntityOptional = contextView.getOrEmpty("user");

                    if (userEntityOptional.isEmpty()) {
                        throw new RuntimeException("user not found"); // Article 에서와 마찬가지로.. 필요 없을듯
                    }

                    return Mono.just(userEntityOptional.get().getProfileImageId());
                })
                .flatMap(this::findById);
    }
}
