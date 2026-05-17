package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub2_coroutine_builder.run_blocking

/**
 * Coroutine builder - runBlocking
 *
 *
 * 한 줄로
 *      runBlocking 은 "호출한 thread 를 막아 두고 coroutine 본문이 끝날 때까지 기다리는 builder".
 *      일반 (blocking) 세계와 coroutine 세계를 잇는 다리 역할.
 *
 *
 * 시그니처
 *      @Throws(InterruptedException::class)
 *      public actual fun <T> runBlocking(
 *          context: CoroutineContext = EmptyCoroutineContext,
 *          block: suspend CoroutineScope.() -> T
 *      ): T {
 *          val contextInterceptor = context[ContinuationInterceptor]
 *          val eventLoop: EventLoop?
 *          val newContext: CoroutineContext
 *          if (contextInterceptor == null) {
 *              // dispatcher 가 안 들어왔으면 caller thread 의 EventLoop 을 만들어 dispatcher 자리에 끼워 넣는다.
 *              eventLoop = ThreadLocalEventLoop.eventLoop
 *              newContext = GlobalScope.newCoroutineContext(context + eventLoop)
 *          } else {
 *              eventLoop = (contextInterceptor as? EventLoop)?.takeIf { it.shouldBeProcessedFromContext() }
 *              newContext = GlobalScope.newCoroutineContext(context)
 *          }
 *          val coroutine = BlockingCoroutine<T>(newContext, currentThread, eventLoop)
 *          coroutine.start(CoroutineStart.DEFAULT, coroutine, block)
 *          return coroutine.joinBlocking()
 *      }
 *
 *
 * launch / async 와의 결정적 차이
 *      1) Receiver 가 없다.
 *          - launch / async 는 CoroutineScope.launch(...) 처럼 scope 의 확장 함수다.
 *          - runBlocking 은 top-level 함수. coroutine context 밖 어디서나 호출 가능 (그래서 진입점에서 쓴다).
 *      2) 호출 thread 를 block 한다.
 *          - launch / async 는 호출 즉시 Job / Deferred 만 주고 caller 가 다음 줄로 진행 (비동기).
 *          - runBlocking 은 본문이 끝날 때까지 caller thread 를 그 자리에 멈춰 두고, 그 thread 위에서 EventLoop 을 돌린다.
 *      3) 반환 타입이 block 의 결과 그 자체.
 *          - launch       : Job
 *          - async        : Deferred<T>
 *          - runBlocking  : T (block 의 반환값을 그대로 돌려줌)
 *      4) Dispatcher 를 명시하지 않으면 자기 자신의 BlockingEventLoop 을 dispatcher 로 갖는다.
 *          - caller thread (보통 main) 가 그대로 event loop thread 가 되어 그 위에서 coroutine 본문이 돌아간다.
 *          - 그래서 출력 thread name 이 흔히 "main @coroutine#1" 처럼 찍힌다 (launch/async 는 보통 worker-N).
 *
 *
 * 내부 동작 (간단 요약)
 *      1) context 에 dispatcher 가 있으면 그걸 쓰고, 없으면 caller thread 의 EventLoop 을 만들어 dispatcher 자리에 끼워 넣는다.
 *      2) 그 newContext 위에 BlockingCoroutine 을 만들어 start.
 *      3) coroutine.joinBlocking() 으로 caller thread 를 block 한 채 본문 / 자식들이 끝나기를 기다린다.
 *          - 이때 EventLoop 이 caller thread 에서 dispatch 된 task 들을 차례로 실행 → suspend / resume 사이클이 같은 thread 위에서 돈다.
 *      4) 본문 (자식 coroutine 들 포함) 이 모두 끝나면 결과 T 를 그대로 반환.
 *
 *
 * 어디서 쓰나
 *      - main 함수에서 coroutine 세계로 들어가는 진입점
 *          fun main() = runBlocking { ... }
 *      - 동기 API 위에서 동기 코드처럼 보이고 싶을 때 (test, CLI 도구 등)
 *      - 일반 비즈니스 / production 비동기 코드에서는 사용 금지.
 *          이미 coroutine 안에 있는데 또 runBlocking 을 부르면 그 호출 thread 를 막아 버려 coroutine 의 의미가 사라진다.
 *          특히 WebFlux 의 event loop / Android main thread 위에서 runBlocking 을 부르면 deadlock 위험이 크다.
 *
 *
 * 부모-자식 관계
 *      - runBlocking 도 builder 이므로 CoroutineBuilder.kt 의 일반 규칙을 그대로 따른다 (newContext 의 Job 이 BlockingCoroutine 의 부모).
 *      - 다만 진입점이라 보통 outer scope 가 없다. GlobalScope.newCoroutineContext(...) 를 거쳐 만들어진 context 위에서 시작한다.
 *          → 그래서 BlockingCoroutine 자체의 부모 Job 은 null 인 경우가 대부분.
 *      - 본문 안의 launch / async 들은 BlockingCoroutine 의 Job 을 부모로 매달리고,
 *          본문이 끝나려면 그 자식들도 모두 끝나야 한다 (structured concurrency 그대로).
 */
private object RunBlockingDescription
