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
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.List;

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
         * input binder 에 kafka 를 연동해놓으면(kafka consumer) 사용자가 등록해놓은 Consumer 빈이 작동할 것이라고 생각해 볼 수 있다.
         *
         * testcode(외부) -> inputDestination -> input binder -> Consumer
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

    @DisplayName("OutputDestination 으로 output binder 가 전송한 메시지 확인 가능")
    @Test
    void sequenceFluxString() {

        /**
         * 아래는 outputDestination 을 통해 메시지가 전송되는 것을 확인해보았다.
         * output binder 에 kafka 를 연동해놓으면(kafka producer) 사용자가 등록해놓은 Supplier 빈이 작동하여 데이터를 kafka 로 전송할 것임을 생각해볼 수 있다.
         *
         * Supplier -> output binder -> outputDestination -> 외부
         * testcode -> outputDestination
         */

        // given
        // spring.cloud.function.definition 에 sequenceFluxString(등록한 스프링 빈, Supplier) 적용 되어있음.
        // naming convention : {cloud function bean 이름}-out-{argument index}
        String outputBinderName = "sequenceFluxString-out-0";
        List<String> expectStrings = List.of("one", "two", "three"); // supplier 에서 생산하는 element

        // when
        // then
        expectStrings.forEach(
                expect -> {

                    // 스프링 빈으로 등록한 Supplier 에서 데이터를 생성하여 output binder 에게 넘긴 것을 outputDestination 에서 받아본다.
                    Message<byte[]> output = outputDestination.receive(0, outputBinderName);

                    String outputMessage = new String(output.getPayload());

                    assertEquals(expect, outputMessage);
                }
        );
    }

    @DisplayName("Input/Output Destination 으로 데이터를 넣으면 받아볼 수 있다.")
    @Test
    void MapFluxStringTest() {

        /**
         * 아래는 InputDestination 으로 데이터를 넣으면 Function 이 동작하여 데이터를 다시 OutputDestination 으로 받아 확인 해볼 수 있다.
         * input / output binder 에 kafka 를 연동해놓으면(kafka consumer / producer or kafka streams?)
         * 사용자가 등록해놓은 Function 빈이 작동하여 데이터를 kafka 로 부터 받고 다시 kafka 로 전송할 것임을 생각해볼 수 있다.
         *
         * testcode(외부) -> inputDestination -> input binder -> Function -> output binder -> outputDestination -> 외부
         * testcode -> outputDestination
         */

        // given
        String expectMessage = "hello, world!";
        String payload = "world";
        GenericMessage<String> input = new GenericMessage<>(payload);

        // spring.cloud.function.definition 에 mapFluxString(등록한 스프링 빈, Function) 적용 되어있음.
        // naming convention : {cloud function bean 이름}-in/out-{argument index}
        String inputBinderName = "mapFluxString-in-0";
        String outputBinderName = "mapFluxString-out-0";


        // when
        inputDestination.send(input, inputBinderName); // 등록된 input binder 로 메시지 전송

        // then
        Message<byte[]> output = outputDestination.receive(0, outputBinderName); // 등록된 output binder 로 부터 데이터를 받아볼 수 있다.
        String outputMessage = new String(output.getPayload());

        assertEquals(expectMessage, outputMessage);
    }
}