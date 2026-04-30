package dev.starryeye.webflux_and_coroutine.sub2_mono_builder

/**
 * sub2 - mono로 반환 (사용법 편)
 *
 * 이 sub 의 한 줄 요약
 *      "이미 시그니처가 Mono<T> 반환으로 굳어진 함수 본문에서 suspend 함수를 호출하고 싶을 때
 *       mono { } 빌더로 감싸면 된다." 가 한 줄 요약.
 *
 *      이 파일(Overview.kt) 에서는 "어떻게 쓰는가" 를 알 수 있다.
 *      "왜 그렇게 동작하는가" 의 내부 구현은 같은 패키지의 [Internals] 에서 다룬다.
 *
 *      Flux 반환 케이스
 *          mono { } 와 똑같은 패턴이라 sub2 에서는 다루지 않는다.
 *          짝꿍 빌더는 flux { } (kotlinx.coroutines.reactor.FluxKt#flux) 이고,
 *          람다 안에서 send(value) 를 여러 번 호출한다는 것만 다르다.
 *          실제 사용 예제는 sub5_real_world_examples 의 FluxStreamExample 참고.
 *
 *      구성
 *          Overview.kt              - 0. 용어, 1. 문제, 2. 해결, 3. 보너스(Reactor Context 자동 전달)
 *          Internals.kt             - 4 ~ 6. mono { } 의 내부 동작, 7. 한 흐름으로 다시 보기
 *          GreetMonoService.kt      - "변경 불가능하다고 가정하는" 외부 인터페이스
 *          GreetMonoServiceImpl.kt  - mono { } 빌더 사용 + main
 *          GreetMonoServiceImpl2.kt - Reactor Context 가 코루틴까지 전달됨을 확인하는 예제 + main
 *
 *      sub1 (sub1_controller_suspend_support) 와의 관계
 *          sub1 은 "컨트롤러 자체를 suspend 로 선언하는 케이스" 였다.
 *              -> Spring WebFlux 가 알아서 suspend 함수를 받아주었음.
 *          sub2 는 "이미 정해진 인터페이스가 Mono 를 반환해야 하는 케이스" 다.
 *              -> mono { } 빌더로 직접 감싸야 한다.
 *          sub1 의 Overview section 1 에서 언급됐던 두 시나리오가 각각 sub1, sub2 에 대응한다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * 0. 시작하기 전에 - 용어 정리
 * ─────────────────────────────────────────────────────────────────────────────
 *      이 sub 에서 새로 등장하거나 강조되는 용어만 정리한다.
 *
 *      mono { } 빌더
 *          위치: kotlinx.coroutines.reactor.MonoKt#mono
 *          (artifact: kotlinx-coroutines-reactor)
 *
 *          public fun <T> mono(
 *              context: CoroutineContext = EmptyCoroutineContext,
 *              block: suspend CoroutineScope.() -> T?
 *          ): Mono<T>
 *
 *          "내부에서 코루틴을 시작해 그 결과를 Mono<T> 로 노출" 시켜주는 코루틴 빌더.
 *          람다는 suspend CoroutineScope 의 확장이라 안에서 suspend 함수를 그대로 호출 가능.
 *
 *      Reactor Context
 *          Reactor 파이프라인을 따라 흐르는 키-값 저장소.
 *          Mono / Flux 의 .contextWrite { it.put(...) } 으로 항목을 넣을 수 있다.
 *
 *      ReactorContext (코루틴 컨텍스트의 키)
 *          위치: kotlinx.coroutines.reactor.ReactorContext
 *          Reactor Context 를 코루틴 컨텍스트(CoroutineContext) 안으로 가져오기 위한 어댑터다.
 *          코루틴 안에서 다음과 같이 꺼내 쓴다.
 *
 *              val ctx: ReactorContext? = coroutineContext[ReactorContext]
 *              val who: String? = ctx?.context?.get<String>("who")
 *
 *      MonoSink
 *          Mono.create { sink -> ... } 에서 받게 되는 객체.
 *          sink.success(value) / sink.error(throwable) / sink.currentContext() 등을 가진다.
 *          mono { } 의 내부 구현이 이걸 통해 코루틴의 결과를 Mono 에 흘려보낸다. (Internals 에서 다시 등장)
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 1. 문제 - Mono 반환 함수 안에서 suspend 함수를 호출할 수 없다
 * ─────────────────────────────────────────────────────────────────────────────
 *      배경
 *          어떤 외부 라이브러리(혹은 회사 내 공용 모듈) 가 다음 인터페이스를 제공한다.
 *
 *              interface GreetMonoService {
 *                  fun findGreet(): Mono<String>
 *              }
 *
 *          이 인터페이스의 시그니처는 우리가 마음대로 바꿀 수 없다고 가정한다.
 *          (이미 너무 많은 곳에서 호출되고 있거나, 외부 라이브러리라서 변경 불가)
 *
 *          그런데 구현 안에서 부르고 싶은 함수는 suspend 다.
 *
 *              private suspend fun greeting(): String {
 *                  delay(100)
 *                  return "hello"
 *              }
 *
 *      잘못된 시도
 *
 *          override fun findGreet(): Mono<String> {
 *              return Mono.just(greeting())          // 컴파일 에러
 *              // 또는
 *              return Mono.fromCallable { greeting() } // 컴파일 에러
 *          }
 *
 *          오류 메시지
 *              "Suspend function 'greeting' should be called only from a coroutine
 *               or another suspend function"
 *
 *          이유
 *              Mono.just / Mono.fromCallable 의 람다는 일반 함수다.
 *              그래서 안에서 suspend 함수를 호출할 수 없다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 2. 해결 - mono { } 빌더로 감싸기
 * ─────────────────────────────────────────────────────────────────────────────
 *      kotlinx-coroutines-reactor 가 제공하는 mono { } 빌더를 쓰면 된다.
 *
 *          override fun findGreet(): Mono<String> {
 *              return mono {
 *                  greeting()  // OK - mono { } 람다 안은 코루틴 빌더 람다
 *              }
 *          }
 *
 *      왜 OK 인가
 *          mono { } 의 람다 시그니처는 "suspend CoroutineScope.() -> T?" 다.
 *          그 자체가 suspend 함수이므로 안에서 다른 suspend 함수를 호출해도 된다.
 *          람다가 끝나면서 반환한 값이 Mono 의 단일 결과로 흘러간다.
 *          (예외/취소가 발생하면 Mono.error 로 흘러간다.)
 *
 *      실행 환경
 *          [GreetMonoServiceImpl] 의 main 을 실행해보면 다음과 비슷한 로그가 찍힌다.
 *
 *              36:10 [DefaultDispatcher-worker-2 @coroutine#1] - greet: hello
 *
 *          포인트
 *              - 스레드 이름이 "DefaultDispatcher-worker-*" 다.
 *                mono { } 빌더에 별도 dispatcher 인자를 안 주면 코루틴의 기본 Dispatcher (Default) 가 쓰인다.
 *                (sub1 의 컨트롤러 케이스에서는 Dispatchers.Unconfined 였다.
 *                 그쪽은 Spring 이 invokeSuspendingFunction 안에서 명시적으로 Unconfined 를 넘겼기 때문.)
 *              - "@coroutine#1" 이라는 코루틴 이름 표식이 함께 찍힌다 = 진짜로 코루틴 위에서 실행됐다.
 *
 *      여기까지가 "쓰는 방법" 이다.
 *      이제 한 가지만 더 보고 가자: Reactor 의 Context 가 mono { } 안의 suspend 함수까지 전달될까?
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 3. 보너스 - Reactor Context 가 자동으로 전달된다
 * ─────────────────────────────────────────────────────────────────────────────
 *      결론부터: 전달된다.
 *      mono { } 빌더는 자기를 구독하는 쪽의 Reactor Context 를 꺼내 코루틴 컨텍스트에 합쳐준다.
 *      그래서 mono { } 안의 suspend 함수에서 그 값을 다시 꺼내볼 수 있다.
 *
 *      검증 코드는 [GreetMonoServiceImpl2] 와 그 안의 main 함수에 있다. 핵심만 옮기면
 *
 *          // greeting() 안에서 Reactor Context 의 "who" 항목을 읽는다.
 *          private suspend fun greeting(): String {
 *              delay(100)
 *              val who = coroutineContext[ReactorContext]
 *                  ?.context
 *                  ?.get<String>("who")
 *                  ?: "world"
 *              return "hello, $who"
 *          }
 *
 *          // main 에서 contextWrite 으로 "who" 를 주입한다.
 *          greetMonoService.findGreet()
 *              .contextWrite { it.put("who", "starryeye") }
 *              .subscribe { greet -> log.info("greet: {}", greet) }
 *
 *      실행하면 다음과 비슷한 로그가 찍힌다.
 *
 *          45:18 [DefaultDispatcher-worker-2 @coroutine#1] - greet: hello, starryeye
 *
 *      "starryeye" 가 보인다 = main 에서 Reactor Context 에 넣은 값이
 *      coroutine 안 suspend 함수까지 정상적으로 도달했다는 뜻.
 *
 *      이렇게 동작하는 이유는 mono { } 의 내부 구현이
 *          1) sink.currentContext() 로 Reactor Context 를 꺼내고
 *          2) 그걸 ReactorContext 어댑터로 감싸 코루틴 컨텍스트에 합치기
 *      때문이다. 자세한 동작은 [Internals] 의 section 5 에서 다룬다.
 *
 *      여기까지가 "사용자가 알아야 할 전부" 다.
 *      "왜 그냥 동작하는가" 의 내부 구현은 [Internals] 에서 이어진다.
 */
