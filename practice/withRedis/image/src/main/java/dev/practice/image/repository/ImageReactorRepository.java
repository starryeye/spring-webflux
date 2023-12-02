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
     * 여기서는 ReactiveRedisTemplate 이 제공하는 ReactiveHashOperations 을 사용한다.
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
        // -> Mono.create 작업 스레드를 호출 스레드와 분리하지 않으면 결국 의미 없다. (현 동작상 의미 없는 코드이다.)
        return Mono.create(sink -> {
            log.info("ImageRepository.findById: {}, tx: {}", id, Thread.currentThread().getName());

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            hashOperations.multiGet(id, List.of("id", "name", "url"))
                    .doOnNext(
                            strings -> log.info("multiGet, strings={}, tx={}", strings, Thread.currentThread().getName())
                    )
                    .subscribe( // todo, subscribe 를 쓰지말고 doOnNext 을 쓰면? 스레드 관점에서 생각해보기
                            // subscribe 를 사용한 순간 blocking 이 되어 버렸다. Mono.create 의 작업 스레드가 multiGet 을 하고 subscribe 까지 수행함.
                            // IO 호출 이후 작업은 콜백(프레임 워크나 라이브러리에게 위임해야함)으로 수행해야 reactive programming 의 의미가 있으므로 지양해야함.
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
    
    public Mono<ImageEntity> save(String id, String name, String url) {

        log.info("save, tx={}", Thread.currentThread().getName());

        Map<String, String> map = Map.of("id", id, "name", name, "url", url);

        return hashOperations.putAll(id, map)
                .doOnNext(sf -> log.info("putAll, tx={}", Thread.currentThread().getName()))
                .then(findById(id));
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
