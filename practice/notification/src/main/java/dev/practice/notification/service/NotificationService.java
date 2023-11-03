package dev.practice.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Service
public class NotificationService {

    private static final Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

    public Flux<String> getMessageFromSink() {

        return sink.asFlux();
    }

    public void addMessageToSink(String notificationMessage) {

        log.info("addNotification v4, message: {}, tx: {}", notificationMessage, Thread.currentThread().getName());

        sink.tryEmitNext(notificationMessage);
    }
}
