package dev.practice.future.repository;

import dev.practice.common.repository.ImageEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ImageFutureRepository {
    private final Map<String, ImageEntity> imageMap;

    public ImageFutureRepository() {
        imageMap = Map.of(
                "image#1000", new ImageEntity("image#1000", "profileImage", "https://dailyone.com/images/1000")
        );
    }

    @SneakyThrows
    public CompletableFuture<Optional<ImageEntity>> findById(String id) {

        // CompletableFuture 를 반환한다. ForkJoinPool 에서 스레드를 할당하여 supplyAsync 를 Caller 관점에서 non blocking 으로 수행한다.
        return CompletableFuture.supplyAsync(() -> {
            log.info("ImageRepository.findById: {}", id);

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return Optional.ofNullable(imageMap.get(id));
        });
    }
}
