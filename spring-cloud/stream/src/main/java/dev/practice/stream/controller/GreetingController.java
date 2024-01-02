package dev.practice.stream.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class GreetingController {


    // StreamBridge 를 이용하면 데이터를 쉽게 넣을 수 있다. (input)
    private final StreamBridge streamBridge;

    @GetMapping("/greeting")
    public void greeting(@RequestParam("message") String message) {


        /**
         * Stream Function 을 등록하면 설정을 통해 간편하게 Spring cloud stream 을 이용할 수 있다. (kafka 연동)
         * 그러나.. 수동으로 특정 input or function binder 에 전달하고 싶을 수 도 있다...
         * 그럴 땐, StreamBridge 를 이용하면 된다.
         *
         * 참고로..
         * StreamFunctionConfigTest.java 에서 이용한 inputDestination, outputDestination 은 Test 용 객체이다.
         */
        streamBridge.send("mapFluxString-in-0", message); // function binder 로 데이터 넣증
    }
}
