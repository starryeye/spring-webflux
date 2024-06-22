package dev.practice.sub2_sequence;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Slf4j
public class FluxCreate {

    /**
     * [7]
     *
     * Flux 가 제공하는 create 메서드에 대해 알아본다.
     */

    @SneakyThrows
    public static void main(String[] args) {


        log.info("start main tx: {}", Thread.currentThread().getName());

        /**
         * create 메서드
         * public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter)
         *
         * - SynchronousSink 가 아닌 FluxSink 를 사용한다.
         *
         * SynchronousSink 와 차이점
         * - "여러 스레드에서 동시에" next, error, complete 를 명시적으로 호출 할 수 있다.
         * - - 따라서 비동기적으로 Flux(흐름) 를 생성하는 것이다.
         *
         * - create 의 인자는 Consumer 이며 인자는 FluxSink 에 해당한다.
         * - - in/out 에 해당하는 값(상태)은 타입 파라미터에 존재하지 않는다.
         * - - 결과 값에 관심이 없는 것이므로 비동기라고 볼 수 있다.
         *
         * sink 는 main, subscribe 는 ForkJoinPool 에서 수행...
         * publishOn, subscribeOn 을 사용하지 않았는데.. subscriber 와 publisher 가 서로다른 스레드에서 수행될 수 있나..
         * -> CompletableFuture 의 runAsync 는 기본으로 ForkJoinPool 에서 수행한다..
         */

        Flux.create(
                        sink -> {

                            log.info("create FluxSink start, tx: {}", Thread.currentThread().getName());

                            CompletableFuture<Void> task1 = CompletableFuture.runAsync( // CompletableFuture 로 비동기 작업 수행
                                    () -> IntStream.range(0, 5)
                                            .forEach(
                                                    i -> {
//                                                        try {
//                                                            Thread.sleep(100);
//                                                        } catch (InterruptedException e) {
//                                                            throw new RuntimeException(e);
//                                                        }
                                                        log.info("CompletableFuture1 value: {}, tx: {}", i, Thread.currentThread().getName());
                                                        sink.next(i);
                                                    }
                                            )
                            );

                            CompletableFuture<Void> task2 = CompletableFuture.runAsync( // CompletableFuture 로 비동기 작업 수행
                                    () -> IntStream.range(5, 10)
                                            .forEach(
                                                    i -> {
//                                                        try {
//                                                            Thread.sleep(200);
//                                                        } catch (InterruptedException e) {
//                                                            throw new RuntimeException(e);
//                                                        }
                                                        log.info("CompletableFuture2 value: {}, tx: {}", i, Thread.currentThread().getName());
                                                        sink.next(i);
                                                    }
                                            )
                            );
                            /**
                             * 위 코드를 보면 두개의 서로 다른 스레드에서 sink.next 를 호출하여 값(상태)을 전달한다.
                             */

                            // CompletableFuture 의 allOf, thenRun 을 활용하여 두개의 작업이 모두 완료되면 sink 로 complete 이벤트를 전달한다.
                            CompletableFuture.allOf(task1, task2)
                                    .thenRun(
                                            () -> {
                                                log.info("CompletableFuture allOf thenRun, tx: {}", Thread.currentThread().getName());
                                                sink.complete();
                                            }
                                    );

                            log.info("create FluxSink end, tx: {}", Thread.currentThread().getName());

                        }
                )
                .subscribe(
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        error -> log.error("subscribe error: {}, tx: {}", error, Thread.currentThread().getName()),
                        () -> log.info("subscribe complete, tx: {}", Thread.currentThread().getName())
                );

        log.info("end main tx: {}", Thread.currentThread().getName());

        // CompletableFuture 데몬 스레드(ForkJoinPool) 종료 방지용
        Thread.sleep(5000);
    }
}
