package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub2_coroutine_builder

/**
 * Coroutine builder
 *
 *
 * 개념
 *      - Coroutine builder 는 CoroutineScope 로부터 Coroutine 을 생성해 주는 함수다.
 *          - 대표적인 builder: launch, async, runBlocking ...
 *          - 모두 CoroutineScope 의 확장 함수 (혹은 receiver 로 CoroutineScope 를 받는 함수) 이다.
 *
 *
 * 동작
 *      1) CoroutineScope 의 coroutineContext 와 builder 인자로 전달된 context 를 merge 해서 newContext 를 만든다.
 *          - sub1_coroutine_context/sub3_elements/sub7_coroutine_dispatcher 에서 본 newCoroutineContext(...) 가 그 동작.
 *          - 합치는 순서: foldCopies(scope context, arg context) 이고 같은 key 는 인자 쪽이 덮어쓴다.
 *          - 합친 결과에 ContinuationInterceptor (dispatcher) 가 하나도 없으면 그제서야 Dispatchers.Default 가 fallback 으로 붙는다.
 *      2) 만들어진 newContext 위에 새 Coroutine 을 생성한다.
 *      3) 생성한 Coroutine 을 start 하고, builder 의 반환값 (Job / Deferred 등) 으로 호출자에게 돌려준다.
 *      결론.
 *          CoroutineScope 가 기존에 가지고있던 CoroutineContext 와 Coroutine builder 의 파라미터로 받은 CoroutineContext 를
 *              merge 해서 최종 코루틴이 생성된다.
 *
 *
 * 부모-자식 관계 (structured concurrency 의 뼈대)
 *      - builder 로 만들어진 Coroutine 의 부모 Job 은 "동작 1) 의 merge 결과 context 안의 Job" 이다.
 *          Coroutine 내부 코드는 대략 이렇게 동작:
 *              init { initParentJob(parentContext[Job]) }   // parentContext = merge 된 newContext
 *
 *      케이스별
 *          (a) builder arg 에 Job 을 안 넘긴 보통의 경우 (대부분의 코드)
 *              -> merge 결과 Job === scope 의 Job   (동일 인스턴스, 손도 안 닿고 통과)
 *              -> 새 Coroutine 의 부모 = scope 의 Job
 *              -> 우리가 흔히 말하는 structured concurrency:
 *                  scope.cancel() 하면 자식들이 함께 cancel 되고, 자식이 다 끝나야 scope 도 완료.
 *
 *          (b) builder arg 에 Job 을 넘긴 경우 (예: launch(SupervisorJob()), withContext(NonCancellable))
 *              -> merge 시 acc.minusKey(Job) 으로 scope 의 Job 이 빠지고 arg 의 Job 이 그 자리를 차지한다.
 *              -> 새 Coroutine 의 부모 = arg 의 Job. scope 의 Job 은 부모 자리에 안 들어간다.
 *              -> 의도적으로 scope 와 lifecycle 을 분리하는 패턴. scope.cancel() 이 이 coroutine 으로 전파되지 않는다.
 *
 *      위험 케이스 메모
 *          - GlobalScope.launch { } 는 GlobalScope.coroutineContext 자체가 EmptyCoroutineContext 라
 *            묶어줄 부모 Job 이 없다. 어떤 scope 의 lifecycle 에도 매달리지 않는 "떠다니는 coroutine" 이 만들어져 leak 위험이 크다.
 *
 *      "scope -> builder -> coroutine" 한 단계마다 Job 트리에 노드가 하나씩 매달린다는 표현은 (a) 의 경우에만 들어맞는다.
 *      (b) 처럼 arg 로 Job 을 갈아끼우면 그 자리에서 Job 트리의 가지가 끊겨 별도 트리로 분기된다.
 *
 *
 * 비동기 동작
 *      - Coroutine builder 로 만든 Coroutine 은 기본적으로 비동기하게 동작한다.
 *          - launch / async 는 호출 즉시 결과를 기다리지 않고 새 실행 흐름을 띄운다.
 *          - 호출 스레드는 builder 가 반환한 Job / Deferred 만 받고 그대로 다음 줄로 진행한다.
 *      - 예외: runBlocking 은 caller thread 를 block 한 채 자식들이 끝나기를 기다린다 (이름 그대로).
 *          test / main 진입점에서 coroutine 세계로 들어가는 다리 역할이지, 일반 비즈니스 코드에서 쓰는 도구는 아니다.
 *
 *
 * 한 줄 요약
 *      Coroutine builder = "CoroutineScope 의 context + 인자 context 를 merge 해 새 Coroutine 을 만들고,
 *                          그 Job 을 merge 결과 Job 의 자식으로 매달아 (보통은 scope 의 Job, 인자에 Job 을 주면 그 Job)
 *                          비동기로 start 시키는 함수".
 */
private object CoroutineBuilderDescription
