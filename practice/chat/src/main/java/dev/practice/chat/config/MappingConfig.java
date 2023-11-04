package dev.practice.chat.config;

import dev.practice.chat.v1.handler.ChatWebSocketHandler;
import dev.practice.chat.v2.handler.ChatWebSocketHandlerV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.Map;

@Configuration
public class MappingConfig {

    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping(
            ChatWebSocketHandler chatWebSocketHandler,
            ChatWebSocketHandlerV2 chatWebSocketHandlerV2
    ) {

        Map<String, WebSocketHandler> urlMapper = Map.of(
                "/chat", chatWebSocketHandler,
                "/chat/v2", chatWebSocketHandlerV2
        );

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(1);
        mapping.setUrlMap(urlMapper);

        return mapping;
    }
}
