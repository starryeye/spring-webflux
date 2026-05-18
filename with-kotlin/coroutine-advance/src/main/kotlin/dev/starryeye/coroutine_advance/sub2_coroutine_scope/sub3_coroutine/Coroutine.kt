package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub3_coroutine

/**
 * Coroutine
 *
 *
 * Coroutine 은 무엇인가
 *      - 우리가 launch / async / runBlocking 으로 만든 결과물 (= 그 builder 들이 내부적으로 생성하는 객체) 들의 공통 부모.
 *      - 모든 Coroutine 은 AbstractCoroutine 을 상속한다.
 *          - StandaloneCoroutine (launch 가 만드는 것)
 *          - DeferredCoroutine    (async 가 만드는 것)
 *          - BlockingCoroutine    (runBlocking 이 만드는 것)
 *          - ScopeCoroutine       (coroutineScope / withContext 등 scoping 함수가 만드는 것)
 *          - LazyStandaloneCoroutine / LazyDeferredCoroutine ...
 *          전부 AbstractCoroutine 의 자손.
 *
 *
 * AbstractCoroutine 의 정의
 *      public abstract class AbstractCoroutine<in T>(
 *          parentContext: CoroutineContext,
 *          initParentJob: Boolean,
 *          active: Boolean
 *      ) : JobSupport(active), Job, Continuation<T>, CoroutineScope {
 *          ...
 *      }
 *
 *      한 클래스가 세 가지 타입을 동시에 구현한다 → Coroutine = Job + Continuation + CoroutineScope.
 *
 *
 * 세 가지 정체성
 *      1) Job
 *          - "작업의 단위" 이자 lifecycle 상태 머신.
 *          - start / cancel 로 상태를 바꿀 수 있고, join 으로 완료 시점을 기다릴 수 있다.
 *          - 부모 Job / 자식 Job 트리에 매달려 structured concurrency 의 뼈대가 된다.
 *
 *      2) Continuation<T>
 *          - "어떻게 재개할지" 를 들고 있는 콜백 표현체.
 *          - 컴파일러가 본문 (suspend lambda) 을 state machine 으로 변환한 뒤,
 *              매 suspend 지점마다 진행 상태를 Continuation 객체에 저장한다.
 *          - suspend 가 풀려 결과가 도착하면 Continuation.resumeWith(...) 으로 다음 상태부터 재개.
 *          - 즉 Coroutine = "실행 중인 비동기 흐름 자체" 라고 봐도 무방.
 *
 *      3) CoroutineScope
 *          - 본문 안에서 builder (launch / async / ...) 를 호출할 수 있게 해 주는 receiver 역할.
 *          - Coroutine 본문 안에서 새 자식 coroutine 을 띄울 수 있고,
 *              그 자식들의 부모 Job 이 이 Coroutine 자신이 된다.
 *          - sub2_coroutine_builder/launch/Launch.kt 의 "parentContext + this" 트릭이 작동하는 토대.
 *
 *
 * 한 마디로
 *      Coroutine 객체 하나가
 *          - 실행 흐름 (Continuation)
 *          - 그 흐름의 lifecycle 손잡이 (Job)
 *          - 그 흐름 안에서 또 자식을 띄울 수 있는 그릇 (CoroutineScope)
 *      세 가지를 동시에 들고 있는 셈.
 *      그래서 builder 가 만들어낸 객체를 "Job" 으로도, "Deferred" 로도, "this 로 들어가 launch 할 수 있는 scope" 로도 다룰 수 있다.
 *
 *
 * 다음 파일 (CoroutineCreateCoroutineExample.kt) 에서 세 정체성이 실제 출력으로 어떻게 드러나는지 확인한다.
 */
private object CoroutineDescription
