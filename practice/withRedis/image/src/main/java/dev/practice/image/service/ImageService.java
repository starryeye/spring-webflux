package dev.practice.image.service;

import dev.practice.image.common.domain.Image;
import dev.practice.image.repository.ImageReactorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageReactorRepository imageReactorRepository;

    public Mono<Image> getImageById(String imageId) {
        return imageReactorRepository.findById(imageId)
                .map(Image::of);
    }

    public Mono<Image> createImage(String id, String name, String url) {

        log.info("createImage, tx={}", Thread.currentThread().getName());

        return imageReactorRepository.save(id, name, url)
                .map(Image::of);
    }
}
