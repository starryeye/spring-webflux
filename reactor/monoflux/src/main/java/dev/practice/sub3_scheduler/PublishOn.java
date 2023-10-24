package dev.practice.sub3_scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class PublishOn {

    /**
     * [8]
     *
     * 지금까지는 subscribeOn 을 통하여 Source 의 실행 스레드에 영향을 주는 코드들을 봤었다..
     * 그리고 publisher 이후의 연산자들도 모두 Source 의 실행 스레드로 실행했었다..
     *
     * publishOn 을 사용하면..
     * - publishOn 이후에 추가되는 연산자들의 실행 스레드에 영향을 주는 방법이다.
     * - 즉, publisher 를 실행하는 스레드와 그 이후 파이프라인(연산자) 가 실행되는 스레드가 다르도록 만드는 방법이다.
     * - - 그래서.. Hot publisher 라 볼 수 있다.
     * - - 하지만, Hot Publisher 는 개념상 스레드 분리 조건은 없지만 publisher, subscriber 간 스레드 분리를 하는 것이 일반적이다.
     *
     * 주의점..
     * - n 개 크기의 스레드 풀을 사용하도록 적용해도.. 하나의 스레드만 지속적으로 작업한다.
     *
     *
     * 즉...
     * - publishOn 은 위치가 중요하고.. subscribeOn 은 source 실행 스레드에 영향을 주므로 위치가 중요하지 않다.
     * - 별도의 publishOn 이 없고.. A->B->C 순으로 연산자가 chaining 되어있으면 스레드도 연속해서 chaining 실행
     */

    @SneakyThrows
    public static void main(String[] args) {


        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.create(
                sink -> {
                    // subscribeOn 으로 publisher 작업에 영향 주는 scheduler 가 지정되지 않았으므로..
                    // publisher 작업은 subscribe 호출 스레드인.. main 에서 진행된다.
                    for (var i = 0; i < 5; i++) {
                        log.info("publisher next: {}, tx: {}", i, Thread.currentThread().getName());
                        sink.next(i);
                    }
                }
        ).publishOn(
                // publishOn 으로 SingleScheduler 가 아래 연산자 부터 적용된다.
                Schedulers.single()
        ).doOnNext(
                // doOnNext1 작업은 SingleScheduler 가 실행한다.
                item -> log.info("doOnNext1 item: {}, tx: {}", item, Thread.currentThread().getName())
        ).publishOn(
                // publishOn 으로 BoundedElasticScheduler 가 아래 연산자 부터 적용된다.
                Schedulers.boundedElastic()
        ).doOnNext(
                // doOnNext2 작업은 BoundedElasticScheduler 가 실행한다.
                item -> log.info("doOnNext2 item: {}, tx: {}", item, Thread.currentThread().getName())
        ).subscribe(
                // 바로 위 연산자가 publishOn 이 아니므로 바로 위 연산자를 실행한 스레드가 subscribe 도 실행한다.
                value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName())
        );

        // main 스레드가 publisher 작업을 실행하고..
        // SingleScheduler 가 doOnNext1 작업을 실행하고..
        // BoundedElasticScheduler 가 doOnNext2 작업과 subscribe 작업을 실행한다.

        // 로그를 봤을 때 특징으로는.. (예측)
        // 서로 다른 스레드가 작업하는 분기점에서는 바로 즉시 로그가 남겨지지 않지만.. (hot publisher)
        // ->ex. 로그상 "publisher next" 이후 "publisher next" 가 바로 나오는 경우 많음
        // 동일한 스레드가 파이프라인을 타고 여러 연산자의 작업을 수행할 때는 .. 즉시 로그가 남겨진다. (cold publisher)
        // ->ex. 로그상 "doOnNext2" 다음 "doOnNext2" 가 절대로 나오지 않고 "subscribe value" 가 나옴
        // 어찌 보면 당연한게.. 두개의 스레드로 분리해서 작업하는 것과 하나의 스레드로 연속적으로 수행하는 것의 차이이므로.. 당연하긴한듯..



        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(1000); // 캐시 스레드 풀은 데몬 스레드이므로 main 을 대기 시킨다.
    }
}
