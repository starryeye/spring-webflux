package dev.practice;

public class App {

    /**
     * Mono, Flux 에 대해 알아본다.
     *
     * Reactive Streams 에 Publisher 를 구현하였다. (Reactor)
     * 즉, Mono 와 Flux 는 Reactive Streams 의 Publisher 역할에 해당한다.
     *
     * Flux
     * - 0 or N 개의 item 을 subscriber 에게 전달
     * - subscriber 에게 onComplete, onError 이벤트를 전달하면 연결이 종료된다.
     * - onComplete 포함, 모든 이벤트가 optional 하기 때문에 다양한 Flux 를 정의할 수 있다.
     * - onComplete 를 호출하지 않으면 무한한 sequence 를 생성할 수 있다.
     * - List 와 성격이 비슷하다.
     *
     * Mono
     * - 0 or 1 rodml item 을 subscriber 에게 전달
     * - subscriber 에게 onComplete, onError 이벤트를 전달하면 연결이 종료된다.
     * - onComplete 포함, 모든 이벤트가 optional 하기 때문에 다양한 Mono 를 정의할 수 있다.
     * - onNext 가 호출되면 곧바로 onComplete 이벤트가 전달된다.
     * - Mono<Void> 를 통해서 특정 사건의 완료를 전달하는 것으로 응용할 수 있다.
     * - Optional 과 성격이 비슷하다.
     */
}
