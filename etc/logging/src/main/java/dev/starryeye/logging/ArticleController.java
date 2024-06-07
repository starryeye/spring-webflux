package dev.starryeye.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ObjectMapper objectMapper;

    @GetMapping("/articles")
    public Mono<ResponseEntity<ArticleResponse>> get() throws JsonProcessingException {

        ArticleResponse articleResponse = new ArticleResponse("title", "content");

        log.info("created article.. article(json) = {}", objectMapper.writeValueAsString(articleResponse));

        return Mono.just(ResponseEntity.ok(articleResponse));
    }
}
