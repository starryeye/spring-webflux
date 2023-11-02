# spring-webflux
Study Spring Webflux

- Spring Webflux -> Reactor Netty
- Reactor Netty -> Reactor -> Reactive Streams
- Reactor Netty -> Netty -> Java NIO + Reactor Pattern

## Before projects
- JavaIO
  - [JavaIO, NIO, AIO, Reactor Pattern, Proactor Project](https://github.com/starryeye/java/tree/main/server/javaio)
- Reactive Streams
  - [Reactive Streams, Cold/Hot publisher Project](https://github.com/starryeye/java/tree/main/reactivestreams/practice/coldandhot)

## projects
- netty
  - netty 로 server, client 구현
- reactor
  - practice/user
    - [CompletableFuture 버전](https://github.com/starryeye/java/tree/main/completablefuture/practice/user) 에서 Reactor lib 사용하여 개선
  - monoflux
    - Reactor 의 Mono, Flux (Reactive Streams Publisher/Subscriber/Subscription 구현체)
- webhandler
  - DispatcherHandler 를 사용하지 않고, WebHandler, WebFilter, WebExceptionHandler 등을 사용하여 서버 만들기
- functionalendpoints
  - Spring reactive stack 에서 제공하는 Functional Endpoints 로 서버 만들기
  - RouterFunction, HandlerFunction
- practice/user, image
  - Spring Webflux 를 사용하여 reactor/practice/user 를 개선
  - Functional Endpoints
  - Annotated Controller
  - WebClient
  - Spring security reactive
