package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.coroutine_scope_function.sub3_coroutine_scope_function

/**
 * Scoping 함수 - coroutineScope
 *
 * Coroutine 에서는 명시적으로 범위를 나누기 위해 coroutineScope 라는 함수를 제공한다.
 * coroutineScope 함수는 부모 scope 의 CoroutineContext 를 이어받는다.
 * coroutineScope 함수는 ScopeCoroutine 을 생성한다.
 * ScopeCoroutine 은 동기적으로 동작한다. (자식 코루틴이 모두 끝날때까지 suspend)
 *
 * 시그니처
 *      public suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R {
 *          contract {
 *              callsInPlace(block, InvocationKind.EXACTLY_ONCE)
 *          }
 *          return suspendCoroutineUninterceptedOrReturn { uCont ->
 *              val coroutine = ScopeCoroutine(uCont.context, uCont)
 *              coroutine.startUndispatchedOrReturn(coroutine, block)
 *          }
 *      }
 *
 *      internal open class ScopeCoroutine<in T>(
 *          context: CoroutineContext,
 *          @JvmField val uCont: Continuation<T>
 *      ) : AbstractCoroutine<T>(context, true, true), CoroutineStackFrame {
 *          ...
 *          final override val isScopedCoroutine: Boolean
 *              get() = true
 *      }
 *

 *
 * 특징
 *      - 외부 scope 의 context 를 그대로 상속하되 Job 만 새로 override 한 ScopeCoroutine 을 만든다.
 *          → 분리된 lifecycle 단위로 그 안의 자식 coroutine 들을 묶어 관리한다.
 *      - block 안에서 launch / async 로 만든 자식 coroutine 들은 이 ScopeCoroutine 의 Job 을 부모로 매달린다.
 *      - 자식이 모두 완료되어야만 coroutineScope 자체가 완료되어 caller 로 resume 된다.
 *
 *
 * 동기적으로 동작한다 (suspend 함수)
 *      - coroutineScope 는 suspend 함수. caller 입장에선 "block 이 끝날 때까지 그 자리에서 suspend 했다가 resume" 한다.
 *      - launch 처럼 떨어져 나가지 않는다. 자식이 다 끝나기 전까지 caller 의 다음 줄로 안 넘어간다.
 *      - 내부 핵심: ScopeCoroutine.startUndispatchedOrReturn — 별도 dispatch 없이 caller 의 continuation 위에서 그대로 시작.
 *
 *
 * 핵심 정리
 *      coroutineScope = "한정된 영역에서 자식 coroutine 들을 묶어 관리하다가, 모두 끝나면 결과 R 을 돌려주는 suspend 함수".
 *      - 부모-자식 lifecycle 묶음을 코드 블록 단위로 보장.
 *      - 새 CoroutineScope 를 만들지만 builder 처럼 비동기로 떨어져 나가지 않는다 (동기적).
 *      - block 의 마지막 표현 값을 그대로 반환하므로 결과 수신도 자연스럽다.
 *
 *
 * 참고. 이름이 비슷한 3가지
 *      kotlinx.coroutines 에는 "CoroutineScope" 라는 이름을 공유하는 3개의 별개 선언이 있다.
 *      셋은 컴파일러 입장에서 완전히 다른 선언
 *
 * CoroutineScope
 *      interface, "coroutineContext 를 들고 있는 그릇" 의 타입 정의
 *      val x: CoroutineScope
 * CoroutineScope(...)
 *      팩토리 함수, 받은 context 에 Job 을 보장해 ContextScope 인스턴스를 만들어 반환
 *      val cs = CoroutineScope(Dispatchers.IO)
 * coroutineScope { }
 *      suspend scoping 함수, block 동안 새 ScopeCoroutine 을 만들고 자식이 다 끝날 때까지 suspend
 *      val r = coroutineScope { launch { ... }; ... }
 *
 *
 *
 */
private object CoroutineScopeFunctionDescription
