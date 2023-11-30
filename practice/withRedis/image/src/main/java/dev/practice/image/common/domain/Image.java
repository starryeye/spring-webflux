package dev.practice.image.common.domain;

import dev.practice.image.common.repository.ImageEntity;
import lombok.Data;

@Data
public class Image {
    private final String id;
    private final String name;
    private final String url;

    public static Image of(ImageEntity entity) {
        return new Image(
                entity.getId(),
                entity.getName(),
                entity.getUrl()
        );
    }
}
