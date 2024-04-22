package dev.practice.sub6_util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class Map {

    /**
     * [1]
     *
     * map, mapNotNull 연산자를 알아본다.
     *
     * map
     * public final <V> Flux<V> map(Function<? super T, ? extends V> mapper)
     * - onNext 이벤트를 받아서 값을 변경하고 downstream 으로 전달한다.
     * - map vs flatMap.. 동기 비동기 측면에서 차이가 있다고 한다.
     *      비동기 IO 를 수행하는 메서드를 map 으로 감싸고 flatMap 으로 감싸고 두 경우를 각각 테스트 해보면서, 어떤 스레드로 동작되는지 확인해볼것
     *      https://www.baeldung.com/java-reactor-map-flatmap
     *      https://stackoverflow.com/questions/49115135/map-vs-flatmap-in-reactor
     *      -> mapvsflatmap project 에 정리함
     *
     * mapNotNull
     * public final <V> Flux<V> mapNotNull(Function <? super T, ? extends V> mapper)
     * - onNext 이벤트를 받아서 값을 변경하고 전달하는데.. 전달될 값이 null 이면 전달하지 않는다.
     * - 원래는 값이 null 로 전달되면 에러이다.
     * - 즉, nullable 값에 대해 처리해주면 좋다.
     */

    public static void main(String[] args) {

        log.info("start main, tx: {}", Thread.currentThread().getName());

        Flux.range(1, 5)
                .map( // map 적용
                        value -> {
                            log.info("map run, return value:{}, tx: {}", value * 2, Thread.currentThread().getName());
                            return value * 2;
                        }
                )
                .doOnNext(
                        value -> log.info("doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe();

        Flux.range(1, 5)
                .mapNotNull( // mapNotNull 적용
                        value -> {
                            if (value % 2 == 0) {
                                log.info("mapNotNull run, return value: {}, tx: {}", value, Thread.currentThread().getName());
                                return value;
                            }
                            log.info("mapNotNull run, return value: null, tx: {}", Thread.currentThread().getName());
                            return null; // mapNotNull 은 null 을 리턴하면 필터되어 downstream 으로 전달 되지 않는다.
                        }
                )
                .doOnNext(
                        value -> log.info("doOnNext value: {}, tx: {}", value, Thread.currentThread().getName())
                )
                .subscribe();

        log.info("end main, tx: {}", Thread.currentThread().getName());
    }
}
