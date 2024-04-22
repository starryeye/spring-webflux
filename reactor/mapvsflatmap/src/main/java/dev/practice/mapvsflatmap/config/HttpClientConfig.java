package dev.practice.mapvsflatmap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public WebClient helloWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8090")
                .build();
    }

    @Bean
    public WebClient worldWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8091")
                .build();
    }
}
