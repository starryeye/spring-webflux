package dev.practice.notification.service;

import dev.practice.notification.port.out.NotificationStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationStream notificationStream;

    private static final Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

    public Flux<String> getMessageFromSink() {

        return sink.asFlux();
    }

    public static Consumer<String> doReceiver() {
        return sink::tryEmitNext;
    }

    public void addMessage(String notificationMessage) {

        log.info("addNotification, message: {}, tx: {}", notificationMessage, Thread.currentThread().getName());

        notificationStream.pushMessage(notificationMessage);
    }
}
