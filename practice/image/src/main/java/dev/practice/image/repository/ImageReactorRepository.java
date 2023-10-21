package dev.practice.image.repository;

import dev.practice.image.common.repository.ImageEntity;
import dev.practice.image.common.repository.UserEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class ImageReactorRepository {
    private final Map<String, ImageEntity> imageMap;

    public ImageReactorRepository() {
        imageMap = Map.of(
                "1", new ImageEntity("1", "profileImage", "https://practice.dev/images/1")
        );
    }

    @SneakyThrows
    public Mono<ImageEntity> findById(String id) {

        // Mono 에서 sequence 를 만드는 create(Consumer<MonoSink<T>> callback) 사용, 비동기로 Mono 를 생성한다.
        return Mono.create(sink -> {
            log.info("ImageRepository.findById: {}, tx: {}", id, Thread.currentThread().getName());

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            ImageEntity image = imageMap.get(id);
            if (image == null) {

                // 이미지가 없으면 에러를 리턴해야하는 상황 가정
                // sink.error 로 Throwable 와 onError 이벤트를 발생 시킨다.
                sink.error(new RuntimeException("image not found"));
            } else {
                // 값 존재, onComplete 이벤트 발생
                sink.success(image);
            }
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
