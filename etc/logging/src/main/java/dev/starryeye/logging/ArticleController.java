package dev.starryeye.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.starryeye.logging.common.ContextMdc;
import dev.starryeye.logging.common.ContextMdcKey;
import dev.starryeye.logging.common.exception.BusinessException;
import dev.starryeye.logging.common.exception.ErrorCode;
import dev.starryeye.logging.common.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class ArticleController {

    private final ObjectMapper objectMapper;

    @GetMapping("/get-1")
    public Mono<ResponseEntity<ArticleResponse>> get1() throws JsonProcessingException {

        ArticleResponse articleResponse = new ArticleResponse("title", "content");

        log.info("get1 articles.. article(json) = {}", objectMapper.writeValueAsString(articleResponse));

        ContextMdc.put(ContextMdcKey.TEST, "test-1");

        return Mono.just(1)
                .publishOn(Schedulers.parallel()) // thread 변경
                .flatMap(value -> {
                    log.info("response article.. 1");
                    return Mono.just(ResponseEntity.ok(articleResponse));
                });

    }

    @GetMapping("/get-2")
    public Mono<ResponseEntity<ArticleResponse>> get2() throws JsonProcessingException {

        ArticleResponse articleResponse = new ArticleResponse("title", "content");

        log.info("get2 articles.. article(json) = {}", objectMapper.writeValueAsString(articleResponse));

        return Mono.just(1)
                .publishOn(Schedulers.parallel()) // thread 변경
                .flatMap(value -> {
                    log.info("response article.. 2");
                    ContextMdc.put(ContextMdcKey.TEST, "test-2");
                    return Mono.just(ResponseEntity.ok(articleResponse));
                });

    }

    @GetMapping("/error-1")
    public Mono<ResponseEntity<ArticleResponse>> getErrors1() {
        log.error("error1 occurred..");
        throw new BusinessException(ErrorCode.BUSINESS_ERROR_CODE_1, "this is business error 1");
    }

    @GetMapping("/error-2")
    public Mono<ResponseEntity<ExceptionResponse>> getErrors2() {
        log.error("error2 occurred..");
        return Mono.just(ResponseEntity.badRequest().body(new ExceptionResponse(ErrorCode.BUSINESS_ERROR_CODE_2.name(), ErrorCode.BUSINESS_ERROR_CODE_2.getDescription())));
    }

    @PostMapping(value = "/new-1", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ArticleResponse>> create1(@RequestBody ArticleRequest request) {

        log.info("new1 request = {}", request);

        return Mono.just(ResponseEntity.ok(new ArticleResponse(request.title(), request.content())));
    }

    @PostMapping(value = "/new-2", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<ResponseEntity<ArticleResponse>> create2(@ModelAttribute ArticleRequest request) {

        log.info("new2 request = {}", request);

        return Mono.just(ResponseEntity.ok(new ArticleResponse(request.title(), request.content())));
    }

    @GetMapping("/new-3")
    public Mono<ResponseEntity<ArticleResponse>> create3(@ModelAttribute ArticleRequest request) {

        log.info("new3 request = {}", request);

        return Mono.just(ResponseEntity.ok(new ArticleResponse(request.title(), request.content())));
    }

    @GetMapping("/no-body")
    public Mono<ResponseEntity<Void>> noBody(@ModelAttribute ArticleRequest request) {

        log.info("request = {}", request);

        return Mono.just(ResponseEntity.ok().<Void>build());
    }

    @GetMapping("/sleep-and-disconnect-1")
    public Mono<ResponseEntity<ArticleResponse>> sleepAndDisconnect1() throws InterruptedException {

        ArticleResponse articleResponse = new ArticleResponse("title", "content");

        return Mono.delay(Duration.ofSeconds(10)) // 체인 안에서 지연중 connection cancel 시(cancel 시그널 전달), 그 즉시 중단되고 map 이하 연산 수행되지 않음.
                .map(tick -> {
                    log.info("response article..");
                    return ResponseEntity.ok(articleResponse);
                });

        /**
         * 동작 과정
         * (1) Mono 생성과 동시에 Subscription 이 생김
         * (2) 10 s 타이머 동안 client connection 연결이 끊기면
         *      Netty -> Reactor 로 cancel 시그널 전달
         *      타이머 dispose -> map 이하 미실행
         */
    }

    @GetMapping("/sleep-and-disconnect-2")
    public Mono<ResponseEntity<ArticleResponse>> sleepAndDisconnect2() throws InterruptedException {

        Thread.sleep(Duration.ofSeconds(10).toMillis());
        // 체인이 생성 및 반환(return Mono.just)되기 전 connection cancel 시, 체인이 없으므로 cancel 무시됨.
        // Netty 에서는 채널을 closed로 표시. 그래도 생성된 체인에 대해서는 subscribe 를 수행한다.

        // Netty 는 controller 로 부터 publisher 를 반환 받고 subscribe 해서 subscription 으로 동작을 수행시키는데..
        // Publisher 를 반환 받지도 않은 상태이므로 subscription 도 없는데 cancel 시그널을 발생 시킬수 조차 없는 것이다.

        ArticleResponse articleResponse = new ArticleResponse("title", "content");

        return Mono.just(ResponseEntity.ok(articleResponse));

        /**
         * 동작 과정
         * (1) 이 10 s 동안은 아직 Publisher, Subscription 모두 없음
         *      연결이 끊겨도 cancel 시그널을 보낼 대상 자체가 없다
         * (2) 10 s 후 Mono.just 반환
         *      WebFlux 가 어쨌든 subscribe 시도
         *      Mono.just 는 onNext -> onComplete 수행
         *      Netty 채널은 이미 CLOSED 상태라 write 실패
         */
    }
}
