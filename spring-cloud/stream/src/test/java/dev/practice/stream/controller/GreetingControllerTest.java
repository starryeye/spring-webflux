package dev.practice.stream.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestChannelBinderConfiguration.class)
@AutoConfigureWebTestClient
@SpringBootTest
class GreetingControllerTest {

    // 참고로 @WebFluxTest 를 사용하면 StreamBridge 의존성 주입이 해결되지 않아서 SpringBootTest 를 이용함

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OutputDestination outputDestination;

    @DisplayName("StreamBridge::send 로 데이터를 넣어서 Stream Function 과 연동할 수 있다.")
    @Test
    void test() {
        /**
         * controller 에서 StreamBridge 를 통해 function binder 로 데이터를 넣고 있다.
         *
         * 테스트 시나리오
         * 1. controller 에 요청하여 데이터를 function binder 로 넣는다. (input)
         * 2. function binder 가 동작하여 데이터가 출력(output) 된다.
         * 3. outputDestination 을 이용하여 출력된 데이터를 확인하여 검증
         */

        // given
        String message = "test";
        String outputBinderName = "mapFluxString-out-0";

        // when
        webTestClient.get()
                .uri("/greeting?message=" + message)
                .exchange()
                .expectStatus().isOk();

        // then
        Message<byte[]> output = outputDestination.receive(0, outputBinderName);
        String outputMessage = new String(output.getPayload());

        assertEquals("hello, " + message + "!", outputMessage);
    }
}