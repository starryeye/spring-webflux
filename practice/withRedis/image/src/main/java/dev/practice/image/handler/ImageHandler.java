package dev.practice.image.handler;

import dev.practice.image.handler.dto.CreateRequest;
import dev.practice.image.handler.dto.ImageResponse;
import dev.practice.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
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

    public Mono<ServerResponse> addImage(ServerRequest serverRequest) {

        log.info("Handler, addImage start, tx={}", Thread.currentThread().getName());

        Mono<ServerResponse> result = serverRequest.bodyToMono(CreateRequest.class)
                .doOnNext(
                        createRequest -> log.info("request bodyToMono, tx={}", Thread.currentThread().getName())
                )
                .flatMap(
                        createRequest -> imageService.createImage(
                                createRequest.getId(),
                                createRequest.getName(),
                                createRequest.getUrl()
                        )
                ).flatMap(
                        image -> ServerResponse.ok()
                                .bodyValue(new ImageResponse(image.getId(), image.getName(), image.getUrl()))
                );

        // request bodyToMono 보다 먼저 찍힘 bodyToMono 는 publisher 를 반환하고 non-blocking 임을 알 수 있다.
        log.info("Handler, addImage end, tx={}", Thread.currentThread().getName());

        return result;
    }
}
