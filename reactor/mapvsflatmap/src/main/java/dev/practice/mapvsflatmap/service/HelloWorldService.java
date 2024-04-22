package dev.practice.mapvsflatmap.service;

import dev.practice.mapvsflatmap.client.HelloClient;
import dev.practice.mapvsflatmap.client.WorldClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelloWorldService {

    private final HelloClient helloClient;
    private final WorldClient worldClient;

    public Mono<String> collect() {


        log.info("before hello request, tx : {}", Thread.currentThread().getName());
        Mono<String> helloPublisher = helloClient.get();

        log.info("before world request, tx : {}", Thread.currentThread().getName());
        Mono<String> worldPublisher = worldClient.get();

        log.info("before zip, tx : {}", Thread.currentThread().getName());
        return Mono.zip(helloPublisher, worldPublisher)
                .map(
                        tuple -> {
                            String zip = tuple.getT1() + ", " + tuple.getT2();

                            log.info("zip : {}, tx : {}", zip, Thread.currentThread().getName());
                            return zip;
                        }
                );
    }
}
