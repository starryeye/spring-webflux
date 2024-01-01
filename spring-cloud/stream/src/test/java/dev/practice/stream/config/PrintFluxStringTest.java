package dev.practice.stream.config;

import dev.practice.stream.service.PrintService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Mock 객체 사용을 위함
public class PrintFluxStringTest {

    /**
     * Unit Test..
     *
     * 스프링을 띄우지 않고 테스트 대상 객체만 생성하여 유닛 테스트를 진행한다.
     * 의존성은 Mocking 한다.
     */

    private StreamFunctionConfig streamFunctionConfig= new StreamFunctionConfig(); // Test target

    @Mock
    private PrintService printService; // target 의 의존성은 Mocking

    @DisplayName("StreamFunctionConfig 에서 등록할 printFluxString 을 검증한다.")
    @Test
    void test() {

        /**
         * printFluxString(Consumer) 를 만들어서 실행(accept) 하면 printService 로 정상적으로 파라미터가 전달되는지 검증
         */

        // given
        List<String> values = List.of("A", "B", "C");
        Flux<String> givenFlux = Flux.fromIterable(values);
        Consumer<Flux<String>> printFluxString = streamFunctionConfig.printFluxString(printService);

        // when
        printFluxString.accept(givenFlux);

        // then
        verify(printService).print("consumer print value : {}", values.get(0)); // mocking 된 printService 로 해당 파라미터가 전달되었는지 검증
        verify(printService).print("consumer print value : {}", values.get(1));
        verify(printService).print("consumer print value : {}", values.get(2));
    }
}
