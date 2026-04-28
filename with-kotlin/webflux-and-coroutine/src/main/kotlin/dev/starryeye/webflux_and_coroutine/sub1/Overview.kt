package dev.starryeye.webflux_and_coroutine.sub1

/**
 * sub1 - Coroutine 사용하기 (사용법 편)
 *
 * 이 sub 의 한 줄 요약
 *      "Spring WebFlux 컨트롤러를 suspend 로 선언하기만 해도 그대로 동작하는 이유" 를
 *      문제 상황 -> Spring 의 지원 -> 내부 동작 순서로 따라간다.
 *
 *      이 파일(Overview.kt) 에서는 "어떻게 쓰는가" 를 알 수 있다.
 *      "왜 그렇게 동작하는가" 의 Spring 내부 구현은 같은 패키지의 [Internals] 에서 다룬다.
 *
 *      구성
 *          Overview.kt   - 0. 용어 정리, 1. 문제, 2. 해결 (사용자 시점)
 *          Internals.kt  - 3 ~ 5. Spring 내부 동작, 6. 한 흐름으로 다시 보기
 *          GreetController.kt - 동작하는 예제 컨트롤러
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * 0. 시작하기 전에 - 용어 정리
 * ─────────────────────────────────────────────────────────────────────────────
 *      Spring WebFlux
 *          Spring 의 비동기/논블로킹 웹 스택. 내부적으로 Project Reactor 를 사용한다.
 *          요청 처리에 적은 수의 이벤트 루프 스레드(보통 reactor-http-nio-*) 만 쓴다.
 *
 *      Project Reactor / Mono / Flux
 *          비동기 결과를 다루기 위한 라이브러리. WebFlux 가 기반으로 쓴다.
 *          Mono<T> = "0개 또는 1개의 비동기 결과", Flux<T> = "0..N 개의 비동기 결과 스트림".
 *
 *      Reactor Context
 *          Reactor 파이프라인을 따라 흐르는 키-값 저장소.
 *          "스레드가 바뀌어도 따라다녀야 하는 부가 정보" 를 담는 용도
 *          (예: trace id, security context 같은 것).
 *
 *      suspend 함수
 *          "도중에 멈췄다가 나중에 이어서 실행될 수 있는" 함수.
 *          예) suspend fun fetchUser(): User { ... }
 *
 *      Continuation
 *          "일시 중단된 코루틴을 다시 이어서 실행하기 위한 콜백 객체".
 *          코틀린 컴파일러는 모든 suspend 함수에 마지막 파라미터로 Continuation 을 슬쩍
 *          끼워 넣는다. (그래서 자바에서 보면 suspend fun foo(): String 이 "Object foo(Continuation) " 처럼 보인다.)
 *          이 사실은 [Internals] 에서 KotlinDetector 와 reflection 호출 부분을 이해할 때
 *          다시 등장한다.
 *
 *      코루틴 (coroutine)
 *          suspend 함수를 실행할 수 있는 "실행 단위".
 *          launch { }, async { }, runBlocking { }, mono { }, future { } 같은
 *          코루틴 빌더로 만들어진다.
 *
 *      Dispatcher
 *          코루틴이 "어떤 스레드에서" 실행될지를 정하는 설정.
 *          Dispatchers.IO, Dispatchers.Default, Dispatchers.Unconfined 등이 있다.
 *
 *      Dispatchers.Unconfined
 *          "별도 스레드로 점프하지 않고 호출한 스레드 위에서 그대로 이어서 실행" 한다는 뜻.
 *          WebFlux 가 코루틴을 돌릴 때 기본적으로 이걸 사용한다.
 *          그래서 컨트롤러 안에서 블로킹 IO 를 하면 이벤트 루프 스레드(reactor-http-nio-*)가
 *          그대로 막혀버린다는 점은 일반 WebFlux 와 동일하게 적용된다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 1. 문제 - suspend 함수는 아무데서나 부를 수 없다
 * ─────────────────────────────────────────────────────────────────────────────
 *      규칙
 *          suspend 함수는 "다른 suspend 함수" 또는 "코루틴 빌더 람다" 안에서만 호출할 수 있다.
 *          그렇지 않으면 컴파일러가 다음 메시지를 띄운다.
 *              "Suspend function 'xxx' should be called only from a coroutine
 *               or another suspend function"
 *
 *      이 규칙 때문에 자주 부딪히는 두 가지 상황을 살펴본다.
 *
 *      (1) 일반 함수 안에서 suspend 함수를 부르고 싶을 때
 *
 *          @RestController
 *          class GreetController {
 *              private suspend fun greeting(): String = "hello"
 *
 *              @GetMapping("/greet")
 *              fun greet(): String {
 *                  return greeting() // 컴파일 에러 (greet 가 일반 함수라서)
 *              }
 *          }
 *
 *          가장 단순한 해결책: greet() 자체를 suspend 로 만든다.
 *          "그렇게 만들어도 Spring 이 알아서 처리해주는가?" 가 바로 다음 섹션의 주제다.
 *
 *      (2) 인터페이스 시그니처가 정해져 있어서 Mono / CompletableFuture 등을 반환해야 할 때
 *
 *          interface GreetMonoService {
 *              fun findGreet(): Mono<String>
 *          }
 *
 *          class GreetMonoServiceImpl : GreetMonoService {
 *              private suspend fun greeting(): String = "hello"
 *
 *              override fun findGreet(): Mono<String> {
 *                  return Mono.fromCallable { greeting() } // 컴파일 에러
 *                  // fromCallable 의 람다는 일반 함수라서 suspend 호출 불가
 *              }
 *          }
 *
 *          해결책: kotlinx-coroutines-reactor 가 제공하는 mono { } 빌더를 쓴다.
 *          (위치: kotlinx.coroutines.reactor.MonoKt#mono - artifact: kotlinx-coroutines-reactor)
 *
 *              override fun findGreet(): Mono<String> = mono { greeting() }
 *
 *          mono { } 람다는 CoroutineScope 의 확장이라 안에서 suspend 함수를 호출해도 OK.
 *          비슷하게 future { } (kotlinx-coroutines-jdk8) 는 CompletableFuture 를 만들어준다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 2. 해결 - WebFlux 컨트롤러는 suspend 함수를 그대로 받는다
 * ─────────────────────────────────────────────────────────────────────────────
 *      Spring WebFlux 는 컨트롤러 핸들러 메서드를 suspend 로 선언하는 것을 그대로 지원한다.
 *      덕분에 별도의 코루틴 빌더 없이 컨트롤러에서 바로 suspend 함수를 호출할 수 있다.
 *
 *          @GetMapping
 *          suspend fun greet(): String {
 *              log.info("context: {}", coroutineContext)
 *              log.info("thread:  {}", Thread.currentThread().name)
 *              return greeting()
 *          }
 *
 *      이 컨트롤러를 호출하면 로그에 다음과 같은 값이 찍힌다.
 *
 *          context: [Context1{reactor.onDiscard.local=...},
 *                    MonoCoroutine{Active}@xxxxxxxx,
 *                    Dispatchers.Unconfined]
 *          thread:  reactor-http-nio-2
 *
 *      각 항목의 의미
 *
 *          Context1{reactor.onDiscard.local=...}
 *              Reactor Context 가 코루틴의 context 로 변환되어 들어와있다는 뜻.
 *              ("Context1" 은 Reactor 가 키-값 1쌍짜리 Context 를 표현하는 내부 클래스 이름이고,
 *               "reactor.onDiscard.local" 은 Reactor 가 자체적으로 넣어둔 기본 항목 중 하나일 뿐이다.
 *               중요한 건 항목 이름이 아니라, Reactor 의 컨텍스트 정보가
 *               suspend 함수 안에서도 그대로 보인다는 점이다.)
 *
 *          MonoCoroutine{Active}@xxxxxxxx
 *              Spring 이 "핸들러 호출을 감싸기 위해" 만든 코루틴의 Job 이다.
 *              컨트롤러가 끝나면 응답 Mono 가 완료되면서 이 Job 도 같이 정리된다.
 *              ("Spring 이 내부적으로 코루틴 한 개를 띄워서 컨트롤러를 그 위에서 돌린다" 는 증거)
 *
 *          Dispatchers.Unconfined
 *              앞서 말한 대로 "스레드 점프 없이 호출 스레드 그대로" 모드.
 *
 *          reactor-http-nio-2
 *              실제 실행 스레드. WebFlux 의 이벤트 루프 스레드 중 하나
 *              (WebFlux 의 기본 서버는 Netty 이고, Netty 의 NIO 이벤트 루프 스레드 이름이
 *               reactor-http-nio-{번호} 형태로 찍힌다).
 *
 *      여기서 외워둘 점
 *          - suspend 컨트롤러는 "사실상 평소의 reactive 컨트롤러와 같은 스레드 모델" 이다.
 *          - 따라서 suspend 컨트롤러 안에서 Thread.sleep, JDBC 등 블로킹 호출을 직접
 *            쓰면 안 된다. 평소처럼 non-blocking 라이브러리를 쓰거나,
 *            정 필요하면 withContext(Dispatchers.IO) { ... } 로 감싸야 한다.
 *
 *      예제 코드는 같은 패키지의 [GreetController] 에 있다.
 *
 *      여기까지가 "사용자가 알아야 할 전부" 다.
 *      "왜 그냥 동작하는가" 의 Spring 내부 구현은 [Internals] 에서 이어진다.
 */
