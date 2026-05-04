package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub2_propagation.sub1_propagation

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext

/**
 * CoroutineContext 전파
 *
 *      suspend 함수 A 가 또 다른 suspend 함수 B 를 호출하는 상황을 생각해본다.
 *
 *          suspend fun outer() {       // 함수 A
 *              ...
 *              nested()                // -> 함수 B 호출
 *          }
 *          suspend fun nested() { ... } // 함수 B
 *
 *      이때 컴파일러는 CPS 변환을 통해 두 함수 모두에 Continuation 을 인자로 받게 만든다.
 *          그리고 outer 가 nested 를 호출할 때, "outer 자기 자신의 Continuation" 을 nested 에게 그대로 넘긴다.
 *              (정확히는 outer 의 Continuation 을 감싼 상태 머신을 nested 에게 전달한다)
 *      Continuation 안에는 CoroutineContext 가 들어 있으므로
 *          -> nested 함수 입장에서도 outer 와 동일한 CoroutineContext 가 보인다.
 *          -> 즉, suspend 함수 호출 체인을 따라 CoroutineContext 가 자연스럽게 "전파" 된다.
 *
 * 특징
 *      - suspend 함수가 호출 체인 어디에 있든 상관없이 항상 같은 CoroutineContext 를 본다.
 *          (Job, CoroutineName, CoroutineDispatcher, Coroutine ID 등이 그대로 보인다)
 *      - suspend 함수 안에서 그냥 coroutineContext 라고 쓰면 (sub3 참고)
 *          "컴파일러가 인자로 받은 Continuation 의 context 를 꺼내" 주기 때문에
 *          호출 체인의 어디에서 찍어도 동일한 값이 나온다.
 *
 *
 * 출력
 *      [main @coroutine#1] - context in outer:
 *          [CoroutineId(1), "coroutine#1":BlockingCoroutine{Active}@..., BlockingEventLoop@...]
 *      [main @coroutine#1] - context in nested:
 *          [CoroutineId(1), "coroutine#1":BlockingCoroutine{Active}@..., BlockingEventLoop@...]
 *
 *      두 줄이 동일하다 -> outer 의 Continuation 을 통해 nested 에게 같은 CoroutineContext 가 전파된 결과.
 */
private val log = KotlinLogging.logger {}

private suspend fun nested() {
    val ctx = coroutineContext
    log.info { "context in nested: $ctx" }
}

private suspend fun outer() {
    val ctx = coroutineContext
    log.info { "context in outer:  $ctx" }
    // 다른 suspend 함수 호출 - outer 의 Continuation 이 그대로 전달되어 CoroutineContext 도 그대로 전파된다.
    nested()
}

fun main() {
    // 일부러 CoroutineName 을 하나 얹어 두면, 두 출력 모두 동일한 CoroutineName 을 들고 있는 것을 확인 가능하다.
    runBlocking(context = CoroutineName(name = "demo")) {
        outer()
    }
}
