package dev.practice.functionalendpoints.withdispatcherhandler;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class GreetingHandler {

    /**
     * Bean 으로 꼭 등록할 필요는 없어보이지만..
     * 보통의 경우 여기서 Service 빈을 주입받아야 하므로.. 등록해야함
     *
     * GreetingHandler 는 HandlerFunction 함수형 인터페이스에 맞는 시그니처를 가지고 있다.
     */

    public Mono<ServerResponse> hello(ServerRequest serverRequest) {

        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
                .bodyValue("Hello world");
    }
}
