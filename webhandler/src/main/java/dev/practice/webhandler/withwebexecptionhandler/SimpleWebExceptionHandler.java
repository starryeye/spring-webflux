package dev.practice.webhandler.withwebexecptionhandler;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;


public class SimpleWebExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        final ServerHttpResponse response = exchange.getResponse();

        if (ex instanceof CustomException) { // 예외 처리

            response.setStatusCode(HttpStatus.BAD_REQUEST);

            DataBuffer responseBody = response.bufferFactory()
                    .wrap(ex.getMessage().getBytes());

            response.getHeaders()
                    .add(HttpHeaders.CONTENT_TYPE, "text/plain;charset=UTF-8"); // For 한글 응답

            return response.writeWith(Mono.just(responseBody)); // 정상 응답 처리
        } else {

            // CustomException 이 아닌 예외라면 다음 ExceptionHandler 로 넘긴다.
            // 모든 ExceptionHandler 가 처리하지 못한 예외는 500 Error 로 응답된다. (by handleUnresolvedError)
            return Mono.error(ex);
        }
    }
}
