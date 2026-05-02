package dev.starryeye.webflux_and_coroutine.sub5_real_world_examples

/**
 * sub5 - sub2 / sub3 / sub4 가 실무에서 어디 나오는가
 *
 * 핵심 한 줄
 *      sub2 (Mono 반환)            : Spring WebFlux 가 시그니처를 강제하는 인터페이스 자리. 특히 WebFilter 가 압도적으로 자주 등장.
 *                                      controller 에서는 필요없는 이유가 HandlerAdapter 가 대신 mono {} 해주고 있기 때문.
 *      Flux 반환                    : sub2 와 같은 패턴(flux { } 빌더). 스트리밍 자리.
 *      sub3 (CompletableFuture 반환) : Java 모듈 / 외부 SDK 가 시그니처를 강제하는 자리.
 *      sub4 (Unit, fire-and-forget) : 감사 로그 / 메트릭 같은 부수 효과 작업.
 *
 * 예제 파일
 *      WebFilterExample      - sub2 사례 (가장 흔함)
 *      FluxStreamExample     - Flux 반환 사례 (R2DBC / SSE / Reactor Kafka 같은 스트리밍 자리)
 *      JavaApiAdapterExample - sub3 사례
 *      DomainEventExample    - sub4 사례
 *
 *
 * 결론
 *      한 문장으로 줄이면
 *          "sub2 / sub3 / sub4 는 결국 '시그니처가 강제되는 자리에 그 시그니처에 맞는 코루틴 빌더를 끼워 넣는다'
 *           라는 한 가지 아이디어다."
 *
 *      어떤 시그니처(Mono / Flux / CompletableFuture / Unit) 가
 *      어떤 빌더(mono / flux / future / launch) 를 쓸지를 결정한다.
 *      그래서 새로운 시그니처를 만나도 패턴은 같고 빌더 이름만 바뀐다.
 */
