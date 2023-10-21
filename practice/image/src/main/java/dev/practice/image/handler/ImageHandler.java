package dev.practice.image.handler;

import dev.practice.image.common.domain.Image;
import dev.practice.image.handler.dto.ImageResponse;
import dev.practice.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ImageHandler {

    private final ImageService imageService;

    public Mono<ServerResponse> getImageById(ServerRequest serverRequest) {

        return imageService.getImageById(serverRequest.pathVariable("imageId"))
                .flatMap(
                        image -> ServerResponse.ok()
                                .bodyValue(new ImageResponse(image.getId(), image.getName(), image.getUrl()))
                ).onErrorMap( // onError 이벤트 발생할 경우. (image 찾지 못하면 발생한다.)
                        // onError 이벤트 발생은 동일, Throwable 변경, HttpStatus 값을 404 로 내린다...
                        // WebExceptionHandler 가 처리한다.
                        e -> new ResponseStatusException(HttpStatus.NOT_FOUND)
                );
    }
}
