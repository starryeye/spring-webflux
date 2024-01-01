package dev.practice.stream.config;

import dev.practice.stream.service.PrintService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.GenericMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@Import(TestChannelBinderConfiguration.class) // binder test 용 import
@SpringBootTest
class StreamFunctionConfigTest {

    @Autowired
    private InputDestination inputDestination;

    @Autowired
    private OutputDestination outputDestination;


    @SpyBean // spy 이므로 stubbing 하지 않는다면 실제 로직이 동작한다.
    private PrintService spyPrintService;

    @DisplayName("InputDestination 으로 input binder 에게 메시지 전송 가능")
    @Test
    void printFluxString() {

        /**
         * 아래는 직접 inputDestination 을 통해 메시지를 전달하는 것을 검증해보았다.
         * inputDestination 에 kafka 를 연동해놓으면(kafka consumer) 사용자가 등록해놓은 Consumer 빈이 작동할 것이라고 생각해 볼 수 있다.
         */

        // given
        String payload = "test";
        GenericMessage<String> inputMessage = new GenericMessage<>(payload);

        // spring.cloud.function.definition 에 printFluxString(등록한 스프링 빈, Consumer) 적용 되어있음.
        // naming convention : {cloud function bean 이름}-in-{argument index}
        String inputBinderName = "printFluxString-in-0";

        // when
        // 등록된 Consumer(inputBinder) 를 직접 실행(accept)하는게 아니라 inputBinder 로 메시지를 전달
        inputDestination.send(inputMessage, inputBinderName); // "consumer print value : test" 로그 찍히는 것도 확인 가능

        // then
        verify(spyPrintService).print("consumer print value : {}", payload); // spy 객체로 해당 파라미터가 전달되는지 검증
    }
}