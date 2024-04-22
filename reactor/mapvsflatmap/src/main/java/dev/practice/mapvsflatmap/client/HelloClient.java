package dev.practice.mapvsflatmap.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class HelloClient {

    private final WebClient helloWebClient;

    public Mono<String> get() {

        return helloWebClient.get()
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(
                        body -> {
                            log.info("body : {}, tx : {}", body, Thread.currentThread().getName());

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
    }
}
