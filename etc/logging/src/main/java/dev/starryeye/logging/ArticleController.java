package dev.starryeye.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.starryeye.logging.common.exception.BusinessException;
import dev.starryeye.logging.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class ArticleController {

    private final ObjectMapper objectMapper;

    @GetMapping
    public Mono<ResponseEntity<ArticleResponse>> get() throws JsonProcessingException {

        ArticleResponse articleResponse = new ArticleResponse("title", "content");

        log.info("created article.. article(json) = {}", objectMapper.writeValueAsString(articleResponse));

        return Mono.just(1)
                .publishOn(Schedulers.parallel()) // thread 변경
                .flatMap(value -> {
                    log.info("response article..");
                    return Mono.just(ResponseEntity.ok(articleResponse));
                });

    }

    @GetMapping("/error")
    public Mono<ResponseEntity<ArticleResponse>> getErrors() {
        log.error("error occurred..");
        throw new BusinessException(ErrorCode.BUSINESS_ERROR_CODE_1, "this is business error 1");
    }
}
