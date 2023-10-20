package dev.practice.webhandler.withwebexecptionhandler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

@Slf4j
public class Server {

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main");

        // WebHandler 생성, 응답 publisher 에서 에러 이벤트 발생
        WebHandler webHandler = new ErrorWebHandler();

        // WebExceptionHandler 생성, 위 WebHandler 에서 발생한 예외를 처리한다.
        WebExceptionHandler exceptionHandler = new SimpleWebExceptionHandler();

        // HttpHandler 생성, WebHandler/WebHandlerException 적용
        final HttpHandler webHttpHandler = WebHttpHandlerBuilder
                .webHandler(webHandler)
                .exceptionHandler(exceptionHandler)
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
