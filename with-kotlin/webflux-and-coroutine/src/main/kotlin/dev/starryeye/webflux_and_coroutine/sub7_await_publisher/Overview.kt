package dev.starryeye.webflux_and_coroutine.sub7_await_publisher

/**
 * sub7 - 반대 방향: suspend 안에서 Publisher (Mono / Flux) 응답 타입을 호출하는 케이스
 *
 * 이 sub 의 위치
 *      sub2 ~ sub4 는 "내가 만드는 함수의 반환 타입이 Mono / Future / Unit 으로 강제되어 있을 때
 *                       그 안에서 suspend 함수를 호출" 하는 방향이었다.
 *      sub7 은 그 반대다.
 *          "내가 짜는 함수는 suspend 인데, 안에서 호출해야 하는 외부 API 가 Mono / Flux 를 반환" 한다.
 *
 *      대표적인 자리
 *          - WebClient.bodyToMono(...) / bodyToFlux(...)     -> Mono<T> / Flux<T> 반환
 *          - Spring Data R2DBC 의 ReactiveCrudRepository       -> Mono<T> / Flux<T> 반환
 *          - Reactor Kafka / RabbitMQ 클라이언트                -> Flux<T> 반환
 *          - Spring Security 의 ReactiveAuthenticationManager -> Mono<Authentication> 반환
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * 핵심 도구 - kotlinx-coroutines-reactor 의 await* 확장 함수
 * ─────────────────────────────────────────────────────────────────────────────
 *      Mono<T> / Flux<T> 에 .awaitXxx() 라는 suspend 확장 함수가 붙어있다.
 *      위치: kotlinx.coroutines.reactor / kotlinx.coroutines.reactive
 *      (artifact: kotlinx-coroutines-reactor)
 *
 *      자주 쓰는 것
 *          Mono<T>.awaitSingle()        : 값 1개 기대 (없으면 NoSuchElementException)
 *          Mono<T>.awaitSingleOrNull()  : 값 0개 또는 1개 (없으면 null)
 *          Flux<T>.awaitFirst()         : 첫 값 (없으면 예외)
 *          Flux<T>.awaitFirstOrNull()   : 첫 값 또는 null
 *          Flux<T>.awaitLast()          : 마지막 값 (스트림 끝까지 소비)
 *          Flux<T>.collectList().awaitSingle() : 모든 값을 List 로 수집
 *          Flux<T>.asFlow()             : Flux 를 코루틴 Flow 로 변환 (스트리밍 그대로 처리)
 *
 *      보통 가장 흔한 두 가지
 *          val user: User = client.get(...).awaitSingle()
 *          val users: List<User> = repo.findAll().collectList().awaitSingle()
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * 예제 파일
 * ─────────────────────────────────────────────────────────────────────────────
 *      AwaitMonoExample.kt
 *          가상의 Mono 반환 메서드를 .awaitSingle() / .awaitSingleOrNull() 로 받는 예제.
 *          (외부 호출 비용 없이 Mono 자체를 임의 생성해서 동작 확인)
 *
 *      WebClientExample.kt
 *          WebClient 가 반환하는 Mono / Flux 를 suspend 함수에서 그대로 await 하는 예제.
 *          (Spring 의 awaitBody / awaitBodilessEntity 같은 확장 함수도 같이 등장)
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * 결론
 * ─────────────────────────────────────────────────────────────────────────────
 *      한 문장으로 줄이면
 *          "외부 API 가 Mono / Flux 를 반환하면 suspend 안에서 .awaitSingle() / .awaitFirstOrNull() / .asFlow() 로 받는다."
 *
 *      대응 관계 (sub2 ~ sub4 와 정확히 반대)
 *          sub2 (Mono 반환 함수 안에서 suspend 호출) <-> sub7 (suspend 함수 안에서 Mono 호출)
 *               해결: mono { greeting() }                  해결: monoCall().awaitSingle()
 *
 *      그래서 라이브러리가 정해놓은 시그니처가 어느 방향이든
 *          "Mono <-> suspend" 변환 한 쌍 (mono { } / awaitSingle()) 만 익히면 거의 다 처리된다.
 */
