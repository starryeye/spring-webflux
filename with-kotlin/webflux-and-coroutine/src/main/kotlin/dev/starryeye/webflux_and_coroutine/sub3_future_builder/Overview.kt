package dev.starryeye.webflux_and_coroutine.sub3_future_builder

/**
 * sub3 - CompletableFuture로 반환 (사용법 편)
 *
 * 이 sub 의 한 줄 요약
 *      "이미 시그니처가 CompletableFuture<T> 반환으로 굳어진 함수 본문에서 suspend 함수를
 *       호출하고 싶을 때 future { } 빌더로 감싸면 된다." 가 한 줄 요약.
 *
 *      이 파일(Overview.kt) 에서는 "어떻게 쓰는가" 를 알 수 있다.
 *      "왜 그렇게 동작하는가" 의 내부 구현은 같은 패키지의 [Internals] 에서 다룬다.
 *
 *      구성
 *          Overview.kt              - 0. 용어, 1. 문제, 2. 해결
 *          Internals.kt             - 3 ~ 4. future { } 빌더의 내부 동작, 5. 한 흐름으로 다시 보기
 *          GreetFutureService.kt    - "변경 불가능하다고 가정하는" 외부 인터페이스
 *          GreetFutureServiceImpl.kt - future { } 빌더 사용 + main
 *
 *      sub1 / sub2 와의 관계
 *          sub2 (Mono) 와 sub3 (CompletableFuture) 는 거의 같은 패턴이다.
 *          반환 타입과 빌더 함수만 다르다.
 *
 *              sub2: override fun findGreet(): Mono<String>             = mono { greeting() }
 *              sub3: override fun findGreet(): CompletableFuture<String> = scope.future { greeting() }
 *
 *          전체 비교 그림은 상위 패키지의 CoroutineUsageGuide 참고.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * 0. 시작하기 전에 - 용어 정리
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *      future { } 빌더
 *          위치: kotlinx.coroutines.future.future
 *          (artifact: kotlinx-coroutines-jdk8)
 *
 *          public fun <T> CoroutineScope.future(
 *              context: CoroutineContext = EmptyCoroutineContext,
 *              start: CoroutineStart = CoroutineStart.DEFAULT,
 *              block: suspend CoroutineScope.() -> T
 *          ): CompletableFuture<T>
 *
 *          "내부에서 코루틴을 시작해 그 결과를 CompletableFuture<T> 로 노출" 시키는 빌더.
 *          람다는 suspend CoroutineScope 의 확장이라 안에서 suspend 함수를 그대로 호출 가능.
 *
 *          mono { } 와의 차이 - 굳이 외워둘 만한 한 가지
 *              mono { }    : top-level 함수. 인자 없이 호출 가능 (내부적으로 GlobalScope).
 *              future { }  : CoroutineScope 의 확장 함수. 그래서 호출하려면 scope 가 있어야 한다.
 *                            예) CoroutineScope(Dispatchers.IO).future { ... }
 *
 *      CoroutineScope
 *          코루틴들이 공유하는 컨텍스트 + 수명 묶음.
 *          sub3 에서는 CoroutineScope(Dispatchers.IO) 형태로 즉석에서 만들어 쓴다.
 *          (실무에서는 보통 액티비티/요청 단위 등 의미 있는 수명에 맞춰 별도로 정의해두고 쓴다.)
 *
 *      Dispatchers.IO
 *          IO bound 작업을 염두에 둔 dispatcher.
 *          내부적으로 Dispatchers.Default 와 같은 워커 풀을 공유하되,
 *          IO 작업이 많이 몰리면 더 많은 스레드까지 늘릴 수 있는 정책을 가진다.
 *          그래서 실제 실행 스레드 이름은 "DefaultDispatcher-worker-*" 로 찍힌다 (Default 와 같은 풀이라서).
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 1. 문제 - CompletableFuture 반환 함수 안에서 suspend 함수를 호출할 수 없다
 * ─────────────────────────────────────────────────────────────────────────────
 *      배경 (sub2 와 거의 같은 구조다. Mono 가 CompletableFuture 로 바뀐 것뿐)
 *
 *          interface GreetFutureService {
 *              fun findGreet(): CompletableFuture<String>
 *          }
 *
 *          이 인터페이스의 시그니처는 우리가 마음대로 바꿀 수 없다고 가정한다.
 *          그런데 구현 안에서 부르고 싶은 함수는 suspend 다.
 *
 *              private suspend fun greeting(): String {
 *                  delay(100)
 *                  return "hello"
 *              }
 *
 *      잘못된 시도
 *
 *          override fun findGreet(): CompletableFuture<String> {
 *              return CompletableFuture.supplyAsync {
 *                  greeting() // 컴파일 에러
 *              }
 *          }
 *
 *          오류 메시지
 *              "Suspend function 'greeting' should be called only from a coroutine
 *               or another suspend function"
 *
 *          이유
 *              CompletableFuture.supplyAsync 의 람다는 일반 함수(Supplier) 다.
 *              그래서 안에서 suspend 함수를 호출할 수 없다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 2. 해결 - future { } 빌더로 감싸기
 * ─────────────────────────────────────────────────────────────────────────────
 *      kotlinx-coroutines-jdk8 가 제공하는 future { } 빌더를 쓰면 된다.
 *
 *          override fun findGreet(): CompletableFuture<String> {
 *              return CoroutineScope(Dispatchers.IO).future {
 *                  greeting()  // OK - future { } 람다 안은 코루틴 빌더 람다
 *              }
 *          }
 *
 *      왜 OK 인가
 *          future { } 의 람다 시그니처는 "suspend CoroutineScope.() -> T" 다.
 *          그 자체가 suspend 함수이므로 안에서 다른 suspend 함수를 호출해도 된다.
 *          람다가 끝나면서 반환한 값이 CompletableFuture 의 결과로 흘러간다.
 *          (예외/취소가 발생하면 future.completeExceptionally 로 흘러간다.)
 *
 *      왜 굳이 CoroutineScope 가 필요한가
 *          future { } 가 CoroutineScope 의 확장 함수라서 그렇다 (위 0번 용어 참고).
 *          mono { } 처럼 그냥 호출이 안 되고, 어떤 scope 위에서 코루틴을 띄울지를 명시해야 한다.
 *          이는 "이 코루틴의 부모/수명을 어디에 묶을지" 를 의식하게 만드는 측면이 있다.
 *
 *      실행 환경
 *          [GreetFutureServiceImpl] 의 main 을 실행해보면 다음과 비슷한 로그가 찍힌다.
 *
 *              53:28 [DefaultDispatcher-worker-1 @coroutine#1] - greet: hello
 *
 *          포인트
 *              - 우리는 Dispatchers.IO 를 넘겼는데 스레드 이름은 "DefaultDispatcher-worker-*" 다.
 *                Dispatchers.IO 가 Dispatchers.Default 와 같은 풀을 공유하기 때문이다 (위 0번 용어 참고).
 *              - "@coroutine#1" 표식이 함께 찍힌다 = 진짜 코루틴 위에서 실행됐다.
 *
 *      여기까지가 "사용자가 알아야 할 전부" 다.
 *      "왜 그냥 동작하는가" 의 내부 구현은 [Internals] 에서 이어진다.
 */
