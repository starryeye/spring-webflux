# spring-webflux
Study Spring Webflux

- Spring Webflux -> Reactor Netty (Project Reactor)
- Reactor Netty (Project Reactor) -> Reactor (Project Reactor) -> Reactive Streams
- Reactor Netty (Project Reactor) -> Netty -> Java NIO + Reactor Pattern
  
## Before projects
- JavaIO
  - [JavaIO, NIO, AIO, Reactor Pattern, Proactor Pattern Project](https://github.com/starryeye/java/tree/main/server/javaio)
- Reactive Streams
  - [Reactive Streams, Cold/Hot publisher Project](https://github.com/starryeye/java/tree/main/reactivestreams/practice/coldandhot)
  
## projects
이론 베이스  
- netty
  - netty 로 server, client 구현
- reactor
  - monoflux
    - Reactor 의 Mono, Flux (Reactive Streams Publisher/Subscriber/Subscription 구현체)
    - 연산자 및 기본 동작 원리 알아보기
- webhandler
  - DispatcherHandler 를 사용하지 않고, WebHandler, WebFilter, WebExceptionHandler 등을 사용하여 서버 만들기
- functionalendpoints
  - Spring reactive stack 에서 제공하는 Functional Endpoints 로 서버 만들기
  - RouterFunction, HandlerFunction
- webclient
  - spring webflux WebClient 알아보기
- websocket
  - WebSocket 통신 프로토콜을 사용하는 간단한 서버 구현
  - SimpleUrlHandlerMapping, WebSocketHandler 활용
- spring-cloud
  - reactive stack + spring cloud
    - spring cloud gateway (reactive)
    - spring cloud circuit breaker (reactive)
    - spring cloud stream (with kafka)
  
## sub projects
실습 베이스(practice 하위에 존재하는 프로젝트)     
- reactor
  - practice/user
    - [CompletableFuture 버전](https://github.com/starryeye/java/tree/main/completablefuture/practice/user) 에서 Reactor lib 사용하여 개선
- practice/user, image
  - Spring Webflux 를 사용하여 reactor/practice/user 를 개선
  - Functional Endpoints
  - Annotated Controller
  - WebClient
  - Spring security reactive
- practice/notification
  - Http streaming 기법을 구현
  - ServerSentEvent, Sinks 활용 (sse)
- practice/chat
  - WebSocketHandler 를 이용한 채팅 서버 구현
  - SimpleUrlHandlerMapping, WebSocketHandler, WebSocketService, Sinks 활용
- practice/withR2dbc/user
  - practice/user 에서 MySQL 을 추가
  - spring data r2dbc 를 사용하여 DB 와 연동
  - reactor-test 를 사용한 비동기 non-blocking 코드 Test
    - Unit test : UserService, AuthService
    - Slice test : UserController, UserR2dbcRepository
    - Integration test : User
- practice/withMongo/chat
  - practice/chat 에서 MongoDB 를 추가
  - spring data mongodb reactive 를 사용하여 연동
  - todo, Test code (mongodb embedded 제공)
- practice/withRedis/image
  - practice/image 에서 Redis 를 추가
  - spring data redis reactive 를 사용하여 연동
- practice/withRedis/notice
  - practice/notice 에서 Redis 를 추가
  - spring data redis reactive 를 사용하여 연동 
  
- 외부 리포지토리
  - [hellogram](https://github.com/starryeye/hellogram)
    - full reactive stack, 모의 sns
  
## Posting
- [Reactive Manifesto](https://starryeye.tistory.com/195)
- [Reactive Programming](https://starryeye.tistory.com/196)
- [Reactive Stream](https://starryeye.tistory.com/197)
- [Java IO](https://starryeye.tistory.com/200)
- [Java NIO](https://starryeye.tistory.com/201)
- [Java NIO Selector](https://starryeye.tistory.com/203)
- [Java NIO Selector epoll](https://starryeye.tistory.com/204)
- [Java AIO](https://starryeye.tistory.com/205)
- [Reactor pattern](https://starryeye.tistory.com/206)
- [Proactor pattern](https://starryeye.tistory.com/207)
- [Netty 1, core](https://starryeye.tistory.com/212)
- [Netty 2, ChannelHandler](https://starryeye.tistory.com/213)
- [Netty 3, serverBootStrap]()
- [WebHandler 와 Netty 의 관계](https://starryeye.tistory.com/219)
- [WebHandler 와 Spring](https://starryeye.tistory.com/220)
- [HttpHandler 생성과 동작](https://starryeye.tistory.com/221)

## todo
- reactor kafka (Project Reactor)
