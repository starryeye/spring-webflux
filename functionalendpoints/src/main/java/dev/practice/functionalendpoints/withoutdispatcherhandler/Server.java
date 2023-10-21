package dev.practice.functionalendpoints.withoutdispatcherhandler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.netty.http.server.HttpServer;

import java.util.function.Consumer;

@Slf4j
public class Server {

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main");

        /**
         * RouterFunction 구현체 생성
         *
         * 참고로 ..
         * 아래
         * RouterFunctions.route() 의 반환 타입인 RouterFunctions.Builder 가 계속 쓰이는 중이다..
         * 중첩 체이닝 때문에 정말 보기 힘든데..
         * RouterFunctions.Builder::path(String pattern, Consumer<RouterFunctions.Builder> builderConsumer)
         * RouterFunctions.Builder::nest(RequestPredicate predicate, Consumer<RouterFunctions.Builder> builderConsumer)
         * RouterFunction.Builder::GET,POST 등의 리턴타입도 RounterFunctions.Builder 이다..
         *
         * 그래서 지옥의 메서드 체이닝이다. 메서드 체이닝도 깊이가 깊고 파라미터가 많고 하면 정말 .. 보기 힘들다..
         * 의미 단위로 끊어서 주석으로 동일한 코드를 만들어 놨다..
         *
         * withdispatcherhandler 에서 처럼 .. 공통 처리를 안하면 좀 간단해질수 있는듯..
         */
        RouterFunction<ServerResponse> router = RouterFunctions.route()
                .path(
                        "/greet", // 공통 prefix url pattern ("/greet" 로 시작해야 통과)
                        routerFunctionBuilder1 -> routerFunctionBuilder1
                                .nest(
                                        RequestPredicates.accept(MediaType.TEXT_PLAIN), // 공통 Predicates 설정 (여기선 accept 가 text/plain 이어야 통과)
                                        routerFunctionBuilder2 -> routerFunctionBuilder2
                                                .GET( // GET 이면 통과
                                                        "/", // "/prefix" + "/" 이면 통과
                                                        RequestPredicates.queryParam("name", name -> !name.isBlank()), // name 쿼리 파라미터의 값이 빈 값이 아니면 통과
                                                        GreetingHandler::greetQueryParam // 해당 HandlerFunction 구현체로 매핑
                                                ).GET( // GET 이면 통과
                                                        "/name/{name}", // "/prefix" + "/name/{name}" 이면 통과
                                                        GreetingHandler::greetPathVariable // 해당 HandlerFunction 구현체로 매핑
                                                ).GET( // GET 이면 통과
                                                        "/header", // "/prefix" + "/header" 이면 통과
                                                        RequestPredicates.headers(h -> h.firstHeader("X-Custom-Name") != null), // header 에 "X-Custom-Name" 값이 있으면 통과
                                                        GreetingHandler::greetHeader // 해당 HandlerFunction 구현체로 매핑
                                                ).POST( // POST 이면 통과
                                                        "/json", // "/prefix" + "/json" 이면 통과
                                                        RequestPredicates.contentType(MediaType.APPLICATION_JSON), // Content-Type 이 application/json 이면 통과
                                                        GreetingHandler::greetJsonBody // 해당 HandlerFunction 구현체로 매핑
                                                ).POST( // POST 이면 통과
                                                        "/text", // "/prefix" + "/text" 이면 통과
                                                        GreetingHandler::greetPlainTextBody // 해당 HandlerFunction 구현체로 매핑
                                                )
                                )
                )
                .build();

//        // 아래와 동일..
//        Consumer<RouterFunctions.Builder> builderConsumerForNest = routerBuilder -> routerBuilder
//                .GET(
//                        "/",
//                        RequestPredicates.queryParam("name", name -> !name.isBlank()),
//                        GreetingHandler::greetQueryParam
//                )
//                .GET(
//                        "/name/{name}",
//                        GreetingHandler::greetPathVariable
//                )
//                .GET(
//                        "/header",
//                        RequestPredicates.headers(h -> h.firstHeader("X-Custom-Name") != null),
//                        GreetingHandler::greetHeader
//                )
//                .POST(
//                        "/json",
//                        RequestPredicates.contentType(MediaType.APPLICATION_JSON),
//                        GreetingHandler::greetJsonBody
//
//                )
//                .POST(
//                        "/text",
//                        GreetingHandler::greetPlainTextBody
//                );
//
//        Consumer<RouterFunctions.Builder> builderConsumerForPath = routerBuilder -> routerBuilder
//                .nest(
//                        RequestPredicates.accept(MediaType.TEXT_PLAIN),
//                        builderConsumerForNest
//                );
//
//        RouterFunctions.Builder route = RouterFunctions.route();
//        route.path("/greet", builderConsumerForPath);
//        RouterFunction<ServerResponse> build = route.build();


        // RouterFunction 구현체를 HttpHandler 로 변환
        HttpHandler httpHandler = RouterFunctions.toHttpHandler(router);

        // ReactorHttpHandlerAdapter 생성
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);

        // HttpServer 생성 및 서버 실행
        HttpServer.create()
                .host("localhost")
                .port(8080)
                .handle(adapter)
                .bindNow()
                .channel().closeFuture().sync();
    }
}
