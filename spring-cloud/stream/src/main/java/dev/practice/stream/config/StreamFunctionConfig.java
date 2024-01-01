package dev.practice.stream.config;

import dev.practice.stream.service.PrintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Configuration
public class StreamFunctionConfig {

    @Bean
    public Consumer<Flux<String>> printFluxString(PrintService printService) {

        /**
         * 함수형 인터페이스 Consumer(T -> X) 를 만들어 빈으로 등록한다.
         *
         * T : Consumer 의 T 로 Flux<String> 를 받아서 subscribe 를 하고 doOnNext 로 PrintService::print 를 수행한다.
         *
         */

        return stringFlux -> stringFlux.subscribe( // flux subscribe
                s -> printService.print("consumer print value : {}", s) // doOnNext(consumer)
        );
    }

    @Bean
    public Supplier<Flux<String>> sequenceFluxString() {

        /**
         * 함수형 인터페이스 Supplier(X -> T) 를 만들어 빈으로 등록한다.
         *
         * T : Supplier 의 T 로 Flux<String> 을 만들어 반환한다.
         */

        return () -> Flux.just("one", "two", "three");
    }
}
