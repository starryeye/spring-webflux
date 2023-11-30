package dev.practice.image.config;

import dev.practice.image.handler.ImageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    /**
     * RouterFunction 등록
     */
    @Bean
    RouterFunction<ServerResponse> router(ImageHandler imageHandler) {
        return RouterFunctions.route().path(
                "/api", // prefix
                builder1 -> builder1.path(
                        "/images", // prefix
                        builder2 -> builder2
                                .GET( // GET
                                        "/{imageId:[0-9]+}", // 0~9 정규 표현식
                                        imageHandler::getImageById // Handler
                                )
                                .POST( // POST
                                        imageHandler::addImage // Handler
                                )
                )
        ).build();
    }
}
