package dev.practice.webhandler.withcodec;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

@Slf4j
public class Server {

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main");

        // WebHandler 생성
        WebHandler webHandler = new WebHandlerWithCodec();

        // HttpHandler 생성
        final HttpHandler webHttpHandler = WebHttpHandlerBuilder
                .webHandler(webHandler)
                .build();

        // ReactorHttpHandlerAdapter 생성
        final ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(webHttpHandler);

        // HttpServer 생성 및 서버 실행
        HttpServer.create()
                .host("localhost")
                .port(8080)
                .handle(adapter)
                .bindNow()
                .channel().closeFuture().sync();
    }
}
