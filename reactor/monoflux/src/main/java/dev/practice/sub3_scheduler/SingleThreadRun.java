package dev.practice.sub3_scheduler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SingleThreadRun {

    /**
     * [1]
     * <p>
     * Reactor 에서..
     * publisher 와 subscriber 가 어느 스레드에서 동작하는지 알아본다.
     * <p>
     * - 기본적으로 publisher 와 subscriber 가 서로 다른 스레드로 동작할 것이라는 생각을 깨부수고 생각하자..
     * 음료수가 담긴 컵에 빨대를 꽂아 놓은 상태를 머리에 두고 생각해보자..
     * publisher 에 subscribe 메서드를 호출하지 않으면 publisher 는 동작하지 않는다...
     * 그리고.. Reactive Streams 에서.. publisher 가 subscriber 의 onNext 를 호출하는 구조이며..
     * 위와 같은 사실을 조금만 생각해보면 publisher 와 subscriber 가 서로 다른 스레드로 동작하는게 아니라..
     * 기본은 결국 같은 스레드로 동작해야함이 효율적임을 알 수 있을 것이다... 요상하다 요상해..
     *
     * 아래 코드의 주석에 숫자를 달아 놨다 순서대로 보며 느껴보자 ex. (1), (2) ...
     */

    public static void main(String[] args) {


        log.info("start main tx: {}", Thread.currentThread().getName());

        ExecutorService executor = Executors.newSingleThreadExecutor();


        try {
            executor.execute( // 참고. submit 과 차이는 반환이 있냐 없냐 차이뿐이다. 둘다 작업큐에 작업을 넣을 뿐이다. execute 이름에 현혹되어 즉시 실행된다고 생각하지 말자..
                    () -> { // executor Runnable

                        // executor 스레드가 작업하는 영역이다.... (1)
                        // 즉, 아래 모든 작업들에 대한 Caller 가 된다.
                        log.info("executor run start, tx: {}", Thread.currentThread().getName());

                        Flux.create(
                                // Publisher 부분이다.
                                // 아무런 설정을 하지 않았으므로 publisher 는 subscribe 를 호출한 스레드에서 실행된다.... (3)
                                sink -> {
                                    for (int i = 1; i <= 5; i++) {
                                        log.info("publisher next: {}, tx: {}", i, Thread.currentThread().getName());
                                        sink.next(i);
                                    }
                                }
                        ).subscribe(
                                // Subscriber 부분이다...
                                // executor 스레드가 publisher(Flux) 를 subscribe 하였다.(executor 스레드가 subscribe 메서드 호출)... (2)
                                // Reactive Streams 구조를 생각해보면 Publisher 가 Subscriber 의 메서드(onNext 등)를 호출한다.
                                // 따라서, Subscriber 는 Publisher 를 수행한 스레드에서 실행된다.... (4)
                                value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName())
                        );

                        log.info("executor run end, tx: {}", Thread.currentThread().getName());
                    }
            );
        } finally {
            executor.shutdown();
        }

        log.info("end main tx: {}", Thread.currentThread().getName());
    }
}
