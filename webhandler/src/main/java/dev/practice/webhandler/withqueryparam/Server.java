package dev.practice.webhandler.withqueryparam;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

@Slf4j
public class Server {

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main");

        WebHandler webHandler = new WebHandlerWithQueryParameter(); // WebHandler 생성

        // HttpHandler 로 생성
        final HttpHandler webHttpHandler = WebHttpHandlerBuilder
                .webHandler(webHandler)
                .build();

        // HttpHandlerAdapter 생성
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
