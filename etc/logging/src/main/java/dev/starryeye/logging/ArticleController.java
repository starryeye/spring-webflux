package dev.starryeye.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.starryeye.logging.common.exception.BusinessException;
import dev.starryeye.logging.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

        log.info("get articles.. article(json) = {}", objectMapper.writeValueAsString(articleResponse));

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

    @PostMapping("/new-1")
    public Mono<ResponseEntity<ArticleResponse>> create1(@RequestBody ArticleRequest request) {

        log.info("created new1 article.. request = {}", request);

        return Mono.just(ResponseEntity.ok(new ArticleResponse(request.title(), request.content())));
    }

    @PostMapping("/new-2")
    public Mono<ResponseEntity<ArticleResponse>> create2(@ModelAttribute ArticleRequest request) {

        log.info("created new2 article.. request = {}", request);

        return Mono.just(ResponseEntity.ok(new ArticleResponse(request.title(), request.content())));
    }

    @GetMapping("/new-3")
    public Mono<ResponseEntity<ArticleResponse>> create3(@ModelAttribute ArticleRequest request) {

        log.info("created new3 article.. request = {}", request);

        return Mono.just(ResponseEntity.ok(new ArticleResponse(request.title(), request.content())));
    }
}
