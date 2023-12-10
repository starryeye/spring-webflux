package dev.practice.user.client;

import dev.practice.user.service.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ImageHttpClient {

    // image server 로 image 정보 요청
    private final WebClient webClient;

    public Mono<ImageResponse> getImageResponseByImageId(String imageId) {

        Map<String, String> urlVariableMap = Map.of("imageId", imageId);

        return webClient.get()
                .uri("/api/images/{imageId}", urlVariableMap)
                .retrieve() // 요청을 서버에 전달, ResponseSpec 을 반환
                .toEntity(ImageResponse.class) // Mono<ResponseEntity<ImageResponse>>
                .map(
                        responseEntity -> responseEntity.getBody()
                );
    }
}
