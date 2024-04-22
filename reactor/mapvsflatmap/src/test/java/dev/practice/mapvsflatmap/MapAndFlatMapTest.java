package dev.practice.mapvsflatmap;

import dev.practice.mapvsflatmap.client.HelloClient;
import dev.practice.mapvsflatmap.client.WorldClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

@Slf4j
@SpringBootTest
public class MapAndFlatMapTest {

    @Autowired
    private HelloClient helloClient;

    @Autowired
    private WorldClient worldClient;

    private MockWebServer mockHelloWebServer;
    private MockWebServer mockWorldWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockHelloWebServer = new MockWebServer();
        mockHelloWebServer.start(8090);
        mockWorldWebServer = new MockWebServer();
        mockWorldWebServer.start(8091);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockHelloWebServer.shutdown();
        mockWorldWebServer.shutdown();
    }

    @DisplayName("flatMap 으로 조합")
    @Test
    void flatMap() {

        /**
         * flatMap 은 비동기이다.. 라고 무작정 받아드리면 큰 오해를 할 수 있다.
         *
         * flatMap 의 파라미터는 Function<? super T, ? extends Mono<? extends R>> transformer 이다.
         * -> 즉, return 으로 publisher 가 들어간다.
         * flatMap 은 단순히 윗 스트림에서 아이템이 내려오면 가지고 있던 publisher 를 진행해오던 스레드로 subscribe 한다. (마블 다이어그램 참고)
         * 이후, 해당 publisher 에서 발생한 아이템을 downstream 으로 전달한다.
         * 해당 publisher 에서 IO 에 의하던간에 다른 스레드로 교체될 수 있다.
         */

        mockHelloWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("Hello"));
        mockWorldWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("World"));

        Mono<String> result = Mono.just(1)
                .doOnNext(
                        item -> log.info("before hello request, tx : {}", Thread.currentThread().getName())
                )
                .flatMap(
                        item -> helloClient.get()
                )
                .doOnNext(
                        item -> log.info("before world request, tx : {}", Thread.currentThread().getName())
                )
                .flatMap(
                        item -> worldClient.get()
                )
                .doOnNext(
                        item -> log.info("after all request, tx : {}", Thread.currentThread().getName())
                );;

        StepVerifier.create(result)
                .expectNext("World")
                .verifyComplete();
    }

    @DisplayName("map 으로 조합")
    @Test
    void map() {

        /**
         * map 은 동기이다.. 라고 받아드리면 큰 오해를 할 수 있다..
         *
         * map 의 파라미터는 Function<? super T, ? extends R> mapper 이다.
         * 즉, return 이 값이다.
         *
         * 해당 예제 코드는 flatMap Test 와 동일한 연산자 조합에 flatMap 을 map 으로 변경한 케이스이다.
         * flatMap 에서와 다르게 return 으로 block 을 이용하게 되었는데 이는 값을 리턴해야하기 때문이다.
         * -> 이 때문에 map 이 동기라는 말이 생긴것이다.
         * -> 즉, map 내부에 처리해야 하는 로직은 값을 리턴해야하므로 내부에서 io 작업이 일어날 시, 동기 블로킹으로 동작 시켜야한다는 것이다.
         *
         * 과거의 "나"는.. flatMap 이 비동기라는 말에 꽂혀서 flatMap 을 사용하면 spring @Async 처럼 새로운 스레드로 동작되나? 아닌데...
         * 이런식으로 스스로 모순적인 생각에 빠지게 됨..
         *
         * 해당 예제 코드의 로그를 보면 더욱 극명하게 나온다.
         * flatMap 에서는 내부에서 io 작업이 비동기로 동작하므로 스레드가 변경되어,
         * doOnNext 에서 찍히는 스레드가 callback 된 스레드로 찍히지만..
         * 여기서는 doOnNext 에 찍히는 스레드는 test worker 로 Mono.just 를 수행한 스레드가 찍힌다.
         *
         *
         * 참고 - block 을 사용한 이유
         * test 코드에서 명시적으로 blocking 임을 강조하고 싶었음..
         * hellClient, worldClient 의 get 메서드 return 타입이 Mono<String> 인데
         * 이를 String 으로 바꾸면 get 메서드 내부에서 block 처리해야함..
         *
         */

        mockHelloWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("Hello"));
        mockWorldWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("World"));

        Mono<String> result = Mono.just(1)
                .doOnNext(
                        item -> log.info("before hello request, tx : {}", Thread.currentThread().getName())
                )
                .map(
                        item -> helloClient.get().block()
                )
                .doOnNext(
                        item -> log.info("before world request, tx : {}", Thread.currentThread().getName())
                )
                .map(
                        item -> worldClient.get().block()
                )
                .doOnNext(
                        item -> log.info("after all request, tx : {}", Thread.currentThread().getName())
                );

        StepVerifier.create(result)
                .expectNext("World")
                .verifyComplete();
    }

    @DisplayName("map 으로 조합2")
    @Test
    void map2() {

        /**
         * 아래 예시는 map 을 사용하지만, helloClient.get 의 리턴 값인 Mono<String> 를(publisher) 그대로 리턴하고
         * flatMap 에 의해 해당 publisher 가 subscribe 된 케이스이다. 동기적인 로직이 아니게 되었다.(map 연산이 끝나는 시점에 결과에 관심 없음, 비동기)
         * -> 즉, map 내부에서 io 작업을 동기적으로 완료시키지 않았고 flatMap 까지 와서 해당 바디 값을 downstream 으로 전달한 것이다.
         *
         * 결론..
         * map 은 단순히 하나의 값을 다른 값으로 변환하는 메서드이다.
         * 따라서, 호출 스레드가 반환까지 책임진다. (아래 코드도 사실 map 만 보면, item 을 받아서 Mono<String> 으로 변환하였고 이는 모두 같은 스레드로 진행되었다.)
         * 그러므로 동기 방식이다. (결과에 관심이 있네..)
         *
         * flatMap 은 반환하는 publisher 에 따라 달라진다.
         * 비동기일 수 도 있고 동기일 수 도 있다.
         * 스레드가 변경되도록 되어있으면 (주로 IO 작업이나 명시적으로 스레드 전환) 비동기 일 것이고
         * 그냥 단순히 Mono.just 와 같은 호출 스레드를 그대로 이어서 진행하도록 되어있으면 동기 일 것이다.
         */

        mockHelloWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("Hello"));
        mockWorldWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("World"));

        Mono<String> result = Mono.just(1)
                .doOnNext(
                        item -> log.info("before hello request, tx : {}", Thread.currentThread().getName())
                )
                .map(
                        item -> helloClient.get()
                )
                .flatMap(flatten -> flatten)
                .doOnNext(
                        item -> log.info("before world request, tx : {}", Thread.currentThread().getName())
                )
                .map(
                        item -> worldClient.get()
                )
                .flatMap(flatten -> flatten)
                .doOnNext(
                        item -> log.info("after all request, tx : {}", Thread.currentThread().getName())
                );;

        StepVerifier.create(result)
                .expectNext("World")
                .verifyComplete();
    }

}
