package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub2_coroutine_builder.launch

/**
 * Coroutine builder - launch (PDF 171p)
 *
 *
 * 시그니처
 *      public fun CoroutineScope.launch(
 *          context: CoroutineContext = EmptyCoroutineContext,
 *          start: CoroutineStart = CoroutineStart.DEFAULT,
 *          block: suspend CoroutineScope.() -> Unit
 *      ): Job {
 *          val newContext = newCoroutineContext(context)
 *          val coroutine = if (start.isLazy)
 *              LazyStandaloneCoroutine(newContext, block) else
 *              StandaloneCoroutine(newContext, active = true)
 *          coroutine.start(start, coroutine, block)
 *          return coroutine
 *      }
 *
 *      public actual fun CoroutineScope.newCoroutineContext(
 *          context: CoroutineContext
 *      ): CoroutineContext {
 *          val combined = foldCopies(coroutineContext, context, true)
 *          ...
 *      }
 *
 *
 * 동작
 *      1) newCoroutineContext(context) 호출
 *          - CoroutineScope 의 coroutineContext 와 인자로 받은 context 를 merge.
 *          - sub2_coroutine_builder 의 CoroutineBuilder.kt 에서 본 그 동작 (foldCopies + dispatcher fallback).
 *      2) StandaloneCoroutine 인스턴스를 생성
 *          - start 가 CoroutineStart.LAZY 이면 LazyStandaloneCoroutine 을 만든다.
 *              -> 호출 즉시 안 돌고, 누군가 start() / join() / await() 를 부를 때 비로소 시작.
 *          - 그 외에는 StandaloneCoroutine 을 만든다 (= 즉시 dispatch).
 *      3) coroutine.start(start, coroutine, block) 으로 본문 실행 시작.
 *      4) 만들어진 StandaloneCoroutine 을 그대로 Job 으로 반환. (자식 코루틴의 Job 이다.)
 *
 *
 * 반환값이 Job 인 이유
 *      - launch 의 반환 타입은 Job 이라, 외부에서 그 Job 으로
 *          - join()   : 완료될 때까지 suspend
 *          - cancel() : 작업 취소
 *          - 상태 조회 (isActive, isCompleted, isCancelled 등)
 *        을 수행할 수 있다.
 *      - StandaloneCoroutine 자체가 Job 을 구현하고 있어서 그대로 Job 타입으로 돌려주면 된다.
 *
 *
 * Lazy 인 경우의 의미
 *      - start = CoroutineStart.LAZY 면 LazyStandaloneCoroutine 이 만들어진다.
 *      - "선언만 해두고, 누군가 .start() / .join() / .await() 를 부를 때까지 실행을 미루는" 변형.
 *      - 결과값이 필요한 시점이 명확할 때 dispatch 비용을 늦추는 용도 정도로 제한적으로 쓰인다.
 *
 *
 * 참고
 *      - launch 는 결과값이 없는 비동기 작업용 (return: Job).
 *      - 결과값이 필요한 비동기 작업은 async (return: Deferred<T>).
 *      - 두 builder 모두 같은 newCoroutineContext 패턴을 거치므로 dispatcher 상속 / Job 부모 결정 규칙은 동일하다.
 */
private object LaunchDescription
