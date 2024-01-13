package dev.practice.webclient.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * [1] WebClient 생성
     * WebClient 는 Thread-safe 하기 때문에 Bean 으로 등록하여 재사용 가능하다.
     *
     * WebClient 생성 2가지
     * - builder 이용
     * - create 이용
     */

    @Bean
    public WebClient helloClient() {
        return WebClient.builder() // builder 이용
                .baseUrl("https://open.er-api.com") // 모든 요청 path 에 prefix 로 적용됨
//                .defaultHeader()
//                .defaultCookie()
//                .filters()
//                .clientConnector()
//                .codecs()
                .build();
    }

    @Bean
    public WebClient worldClient() {

        // 위와 동일한 설정을 가진 WebClient 이다.
        // private final WebClient helloClient, worldClient; 로 하면 각각 DI 된다.
        return WebClient.create("https://open.er-api.com"); // create 이용
    }
}
