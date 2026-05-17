package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub2_coroutine_builder.async

/**
 * Coroutine builder - async
 *
 * 사실상 launch 에서 반환값을 받을 수 있게 된것.
 *
 *
 * 시그니처
 *      public fun <T> CoroutineScope.async(
 *          context: CoroutineContext = EmptyCoroutineContext,
 *          start: CoroutineStart = CoroutineStart.DEFAULT,
 *          block: suspend CoroutineScope.() -> T
 *      ): Deferred<T> {
 *          val newContext = newCoroutineContext(context)
 *          val coroutine = if (start.isLazy)
 *              LazyDeferredCoroutine(newContext, block) else
 *              DeferredCoroutine<T>(newContext, active = true)
 *          coroutine.start(start, coroutine, block)
 *          return coroutine
 *      }
 *
 *      public interface Deferred<out T> : Job {
 *          public suspend fun await(): T
 *      }
 *
 *
 * 동작
 *      1) newCoroutineContext(context) 호출
 *          - CoroutineScope 의 coroutineContext 와 인자로 받은 context 를 merge.
 *          - launch 와 동일한 흐름 (foldCopies + dispatcher fallback 등).
 *      2) DeferredCoroutine<T> 인스턴스를 생성
 *          - start 가 CoroutineStart.LAZY 이면 LazyDeferredCoroutine 을 만든다.
 *              -> 호출 즉시 안 돌고, 누군가 start() / await() / join() 을 부를 때 비로소 시작.
 *          - 그 외에는 DeferredCoroutine 을 만든다 (= 즉시 dispatch).
 *      3) coroutine.start(start, coroutine, block) 으로 본문 실행 시작.
 *      4) 만들어진 DeferredCoroutine 을 그대로 Deferred<T> 로 반환.
 *
 *
 * launch 와의 차이
 *      - block 의 반환 타입이 다르다.
 *          launch: suspend CoroutineScope.() -> Unit       (결과값 없음)
 *          async : suspend CoroutineScope.() -> T          (결과값 T 가 있음)
 *      - builder 의 반환 타입도 다르다.
 *          launch: Job
 *          async : Deferred<T>
 *      - 부모 자식 관계 / dispatcher 결정 규칙 / Job 트리 처리 등은 완전히 동일하다.
 *
 *
 * Deferred 가 Job 을 상속하는 의미
 *      - Deferred 는 "결과 값을 가진 Job".
 *      - Job 인 만큼 cancel(), join(), 상태 조회 (isActive 등) 가 그대로 가능하다.
 *      - 거기에 더해 await() : suspend fun await(): T 가 추가됨.
 *          - 본문 (block) 이 끝날 때까지 suspend 했다가, block 의 반환값을 돌려준다.
 *          - 본문이 예외로 끝났다면 await() 가 그 예외를 다시 던진다 (작업 실패를 호출자가 받게 됨).
 *
 *
 * Lazy 인 경우의 의미
 *      - start = CoroutineStart.LAZY 면 LazyDeferredCoroutine 이 만들어진다.
 *      - 결과값이 필요한 시점이 명확할 때 dispatch 비용을 늦추는 용도.
 *          val d = async(start = CoroutineStart.LAZY) { heavy() }
 *          ...
 *          d.await()   // 이 시점에 비로소 실행 시작
 *
 *
 * 참고
 *      - launch 는 "fire-and-forget" 비동기 작업 (결과 안 받음).
 *      - async 는 "동시에 여러 작업을 띄우고 나중에 결과 모으기" 패턴에 어울린다.
 *          val a = async { fetchA() }
 *          val b = async { fetchB() }
 *          val (ra, rb) = a.await() to b.await()
 *      - structured concurrency 측면에선 둘 다 같은 부모-자식 규칙을 따른다 (sub2_coroutine_builder/CoroutineBuilder.kt 참고).
 */
private object AsyncDescription
