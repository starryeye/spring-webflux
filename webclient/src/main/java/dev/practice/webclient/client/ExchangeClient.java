package dev.practice.webclient.client;

import dev.practice.webclient.response.ExchangeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExchangeClient {

    private final WebClient helloClient; // hello
    private final WebClient worldClient; // world

    /**
     * 연산자는 그냥 WebClient 에 대고 순서대로 적으면 된다.
     * 1. HttpMethod 이름을 적고
     * 2. 요청 데이터 혹은 요청 헤더 등을 설정
     * 3. retrieve 연산자 호출
     * 4. 응답 데이터에 대한 설정
     *
     * 워낙 가독성과 연산자에 대한 직관성이 좋아서 따로 정리하지 않아도 될듯
     */

    public Mono<ExchangeResponse> helloCall() {

        return helloClient.get()
                .uri("/v6/latest")
//                .accept(MediaType.APPLICATION_JSON) // 요청 설정
//                .cookie()
//                .headers()
//                .attributes()
                .retrieve() // 요청!
//                .onStatus( // 응답 설정
//                        HttpStatusCode::is4xxClientError,
//                        resp -> {
//                            log.error("ClientError = {}", resp.statusCode());
//                            return Mono.error(new RuntimeException("ClientError"));
//                        }
//                )
                .bodyToMono(ExchangeResponse.class);
    }

    public Mono<ExchangeResponse> helloCall2() {

        return helloClient.get()
                .uri("/v6/latest")
                .retrieve()
                .toEntity(ExchangeResponse.class)
                .mapNotNull(
                        responseEntity -> { // 응답 데이터 접근 ResponseEntity<ExchangeResponse>

                            HttpHeaders headers = responseEntity.getHeaders();
                            HttpStatusCode statusCode = responseEntity.getStatusCode();
                            return responseEntity.getBody();
                        }
                );
    }

    public Mono<ExchangeResponse> worldCall() {

        return worldClient.get()
                .uri("/v6/latest")
                .exchangeToMono( // 요청과 함께 응답을 설정
                        clientResponse -> { // 응답 데이터 접근
                            ClientResponse.Headers headers = clientResponse.headers();
                            HttpStatusCode httpStatusCode = clientResponse.statusCode();
                            return clientResponse.bodyToMono(ExchangeResponse.class);
                        }
                );
    }
}
