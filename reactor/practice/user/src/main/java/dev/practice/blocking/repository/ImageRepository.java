package dev.practice.blocking.repository;

import dev.practice.common.repository.ImageEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class ImageRepository {
    private final Map<String, ImageEntity> imageMap;

    public ImageRepository() {
        imageMap = Map.of(
                "image#1000", new ImageEntity("image#1000", "profileImage", "https://practice.dev/images/1000")
        );
    }

    @SneakyThrows
    public Optional<ImageEntity> findById(String id) {
        log.info("ImageRepository.findById: {}", id);

        Thread.sleep(1000); // 1초 지연

        // 주어진 id 가 imageMap 에 key 로 없으면 Optional empty 리턴
        return Optional.ofNullable(imageMap.get(id));
    }
}
