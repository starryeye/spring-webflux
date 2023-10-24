package dev.practice.sub3_scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ExecutorServiceScheduler {

    /**
     * [7]
     *
     * 이미 존재하는 ExecutorService 를 Scheduler 로 변환하여 사용
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        // ExecutorService 를 Scheduler 로 사용하기 위해 생성
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        for (int i = 0; i < 100; i++) {

            final int idx = i;

            Flux.create(

                    sink -> {

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        // ExecutorService 로 publisher 작업을 실행
                        log.info("publisher next: {}, tx: {}", idx, Thread.currentThread().getName());
                        sink.next(idx);
                    }
            ).subscribeOn(
                    Schedulers.fromExecutorService(executorService) // 이미 존재하는 ExecutorService 를 Scheduler 로 사용
            ).subscribe(
                    value -> {
                        // ExecutorService 로 subscribe 작업을 실행
                        log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName());
                    }
            );
        }

        executorService.shutdown(); // ExecutorService 스레드 graceful shutdown

        // 하나의 스레드(ExecutorService 의 싱글 스레드)로 100 개의 publisher, subscribe 작업을 수행한다. (1개로 100개를 순차적으로 수행)

        // 참고로 직접 만든 스레드는 데몬 스레드가 아니므로 main 스레드는 미리 종료되어도 작업이 종료 될때 까지 작업을 지속함
        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
