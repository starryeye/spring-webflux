package dev.practice.sub11_cancel_signal;

import lombok.SneakyThrows;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CancelDuringChain {

    @SneakyThrows
    public static void main(String[] args) {

        Mono<String> chain = Mono.delay(Duration.ofSeconds(10))
                .doOnSubscribe(s -> log("subscribed"))
                .doOnCancel(() -> log("CANCEL signal received"))
                .doOnNext(v -> log("onNext -> " + v))
                .map(v -> "OK");

        // Disposable 은 Subscription 을 취소 시킬수 있는 wrapper 객체이다. (취소 전용 subscription)
        Disposable d = chain.subscribeOn(Schedulers.single()) //chain 은 single thread 로 동작시킨다.
                .subscribe(
                        v -> log("received: " + v),
                        e -> log("error   : " + e),
                        () -> log("complete"));

        // 3초 후 “연결 끊김”을 가정해 cancel
        Schedulers.parallel().schedule(() -> {
            log(">>> disposing subscription (connection closed)");
            d.dispose(); // 내부에서 subscription 으로 cancel 호출
        }, 3, TimeUnit.SECONDS);

        // 메인 스레드 종료 방지
        Thread.sleep(6_000);
    }

    private static void log(String msg) {
        System.out.printf("[%5.1fs][%s] %s%n",
                (System.currentTimeMillis() % 100_000) / 1000.0,
                Thread.currentThread().getName(),
                msg);
    }
}
