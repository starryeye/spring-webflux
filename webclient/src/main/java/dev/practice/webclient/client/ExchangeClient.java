package dev.practice.webclient.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Service
public class ExchangeClient {

    private final WebClient helloClient; // hello
    private final WebClient worldClient; // world
}
