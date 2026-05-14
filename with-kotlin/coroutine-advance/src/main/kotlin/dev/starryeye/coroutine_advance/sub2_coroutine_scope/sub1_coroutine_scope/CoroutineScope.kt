package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub1_coroutine_scope

/**
 * CoroutineScope
 *
 *
 * Q. CoroutineScope 는 무엇이고, Coroutine 은 또 무엇인가?
 *      - Coroutine: "한 번의 비동기 작업 단위". launch / async / runBlocking 등으로 시작되는 한 덩어리.
 *      - CoroutineScope: 그 Coroutine 들을 묶어서 lifecycle 을 관리하는 그릇.
 *      - 즉 "작업 자체 (Coroutine)" vs "그 작업들의 group lifecycle 관리자 (CoroutineScope)" 의 관계.
 *
 *
 * CoroutineScope 의 정의
 *      public interface CoroutineScope {
 *          public val coroutineContext: CoroutineContext
 *      }
 *
 *      - 인터페이스 자체는 coroutineContext 프로퍼티 하나뿐.
 *      - 그러나 이 context 가 scope 의 모든 정체성이다 (dispatcher, Job, CoroutineName 등을 들고 다님).
 *
 *
 * CoroutineScope 가 하는 일
 *      - 여러 자식 coroutine 들에 대한 "scope (생애 범위)" 를 정의한다.
 *      - 한 scope 안에는 여러 coroutine 이 포함될 수 있다.
 *      - scope 는 자식 coroutine 들의 생명주기를 관리한다.
 *          (a) 자식들이 전부 끝나야 scope 도 "완료" 상태가 된다.
 *          (b) scope 가 cancel 되면 그 안의 자식 coroutine 들도 함께 cancel 된다.
 *      이런 lifecycle 묶음 관리가 곧 structured concurrency.
 *      - CoroutineScope 에는 CoroutineContext 가 있기 때문에 자식 코루틴들에게 CoroutineContext 가 쭉 전파될 수 있다.
 *
 *
 * 그래서 coroutineContext 에는 반드시 Job 이 있어야 한다.
 *      - "자식들의 lifecycle 을 묶어서 관리" 하려면 부모 노릇을 할 무언가가 필요하고,
 *          그 부모 노릇을 하는 element 가 Job 이다.
 *      - scope 의 Job 이
 *          (a) 새로 만들어지는 자식 coroutine 의 부모 Job 이 되고,
 *          (b) cancel / completion 신호를 자식들에게 전파하는 채널이 된다.
 *      - 그래서 CoroutineScope 의 coroutineContext 에는 사실상 Job 이 반드시 들어가야 한다.
 *          이걸 강제로 보장해 주는 곳이 다음 파일에서 보는 CoroutineScope() 팩토리 함수.
 *
 *
 * 다음 파일에서 이어서 본다.
 *      - CoroutineScopeFunction.kt        : CoroutineScope() 팩토리 함수의 동작
 *      - CoroutineScopeCreateExample.kt   : 실제 생성/구현체 확인 예제
 */
private object CoroutineScopeDescription
