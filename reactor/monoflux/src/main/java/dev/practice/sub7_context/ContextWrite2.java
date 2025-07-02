package dev.practice.sub7_context;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

@Slf4j
public class ContextWrite2 {

    /**
     * [3-add]
     *
     * 파이프라인 중간에 context 에 값을 쓰는 방법에 대해 알아본다.
     *
     * 특이점
     * - contextWrite 는 "위" 로 영향을 준다...
     * -> 그래서, publisher 에서 contextWrite 통해 수정한 값이 읽힌다.
     *
     * -> 아래방향으로.. 변경된 값을 적용하고 싶다면?
     * context 의 value 에 해당하는 객체를 변경하지 말고.. 얕은 복사를 이용해서 value 객체(Item) 의 내부 값만 변경하면
     * Context 를 이용하면서 비슷한 효과를 거둘 수 있다.
     */

    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        // Context 생성
        Context initialContext = Context.of("item", new Item("candy1"));

        Flux.create(
                        fluxSink -> {
                            ContextView contextView = fluxSink.contextView(); // context 에 접근
                            Item item = contextView.get("item");
                            // candy1 출력됨
                            log.info("publisher contextView name: {}, tx: {}", item.getName(), Thread.currentThread().getName());

                            // candy2 로 변경
                            item.setName("candy2"); // contextView.set() 이 없어서 객체(Item) 내부 참조 변수(name)를 이용해 우회한다.

                            fluxSink.next(item.getName());
                            fluxSink.complete();
                        }
                ).publishOn(
                        Schedulers.parallel() // 스레드 변경되더라도 context 는 전파되므로, ThreadLocal 처럼 전파되지 않는 문제 X
                ).flatMap(
                        value -> Flux.create(
                                fluxSink -> {
                                    ContextView contextView = fluxSink.contextView(); // context 에 접근
                                    Item item = contextView.get("item");
                                    // candy2 출력됨
                                    log.info("publisher2 contextView name: {}, tx: {}", item.getName(), Thread.currentThread().getName());

                                    fluxSink.next(item.getName());
                                    fluxSink.complete();
                                }
                        )
                )
                .subscribe(
                        // value 는 downstream 으로 잘 전달될 것 이므로.. publisher2 에서 전달한 starryeye1 이다.
                        value -> log.info("subscribe value: {}, tx: {}", value, Thread.currentThread().getName()),
                        null,
                        null,
                        initialContext
                );

        log.info("end main, tx: {}", Thread.currentThread().getName());

        Thread.sleep(1000); // 캐시 스케줄러는 데몬 스레드
    }

    private static class Item {

        private String name;

        public Item(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
