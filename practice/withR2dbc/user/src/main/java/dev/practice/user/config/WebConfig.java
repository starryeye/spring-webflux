package dev.practice.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {

    @Bean
    public WebClient imageWebClient(
            @Value("${image.server.url:http://localhost:8081}") String imageServerUrl
    ) {
        return WebClient.create(imageServerUrl);
    }
}
