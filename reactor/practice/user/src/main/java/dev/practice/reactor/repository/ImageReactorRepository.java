package dev.practice.reactor.repository;

import dev.practice.common.repository.ImageEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ImageReactorRepository {
    private final Map<String, ImageEntity> imageMap;

    public ImageReactorRepository() {
        imageMap = Map.of(
                "image#1000", new ImageEntity("image#1000", "profileImage", "https://dailyone.com/images/1000")
        );
    }

    @SneakyThrows
    public Mono<ImageEntity> findById(String id) {

        // Mono 에서 sequence 를 만드는 create(Consumer<MonoSink<T>> callback) 사용, 비동기로 Mono 를 생성한다.
        return Mono.create(sink -> {
            log.info("ImageRepository.findById: {}", id);

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            ImageEntity image = imageMap.get(id);
            if(image == null) {

                // 이미지가 없으면 에러를 리턴해야하는 상황 가정
                // sink.error 로 Throwable 와 onError 이벤트를 발생 시킨다.
                sink.error(new RuntimeException("image not found"));
            } else {
                // 값 존재, onComplete 이벤트 발생
                sink.success(image);
            }
        });
    }
}
