package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_coroutine_context_access

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * CoroutineContext 에 접근하는 3가지 방법
 *
 *      sub2 에서 "CoroutineContext 는 Continuation 을 통해 모든 suspend 함수까지 전파된다" 를 봤다.
 *      그렇다면 실제 코드에서 우리는 어떻게 그 context 에 손을 댈 수 있을까?
 *      kotlinx.coroutines / kotlin.coroutines 는 "위치별로" 다음 3가지 접근 방법을 제공한다.
 *
 *      (1) CoroutineScope 내부 - this.coroutineContext
 *              public interface CoroutineScope {
 *                  public val coroutineContext: CoroutineContext
 *              }
 *          runBlocking, launch, async 같은 coroutine builder 안의 람다는
 *              receiver 가 CoroutineScope 이므로
 *              this.coroutineContext (또는 그냥 coroutineContext) 로 접근할 수 있다.
 *
 *      (2) Continuation 내부 - continuation.context
 *              public interface Continuation<in T> {
 *                  public val context: CoroutineContext
 *              }
 *          suspendCoroutine { cont -> ... } 처럼 직접 Continuation 을 다루는 자리에서는
 *              cont.context 로 접근할 수 있다.
 *
 *      (3) suspend 함수 내부 - top-level property coroutineContext
 *              public suspend inline val coroutineContext: CoroutineContext
 *                  get() { throw NotImplementedError("Implemented as intrinsic") }
 *          suspend 함수 안에서는 그냥 coroutineContext 라고만 쓰면 된다.
 *              컴파일러가 intrinsic 으로 처리해서 현재 Continuation 의 context 를 꺼내준다.
 *              (= "Implemented as intrinsic" 의 의미)
 *
 *
 * 정리
 *      위치마다 키워드는 다르지만 결국 같은 CoroutineContext 를 다른 통로로 꺼내는 것뿐이다.
 *      (1) (2) (3) 모두 "현재 코루틴의 Continuation 이 들고 있는 context" 와 동일하다.
 */
private val log = KotlinLogging.logger {}

private suspend fun child() {
    // (3) suspend 함수 내부 - top-level property coroutineContext 로 접근
    //      coroutineContext 는 suspend val 이라 일반 람다 ({ ... }) 안에서는 직접 못 쓴다.
    //          (kotlin-logging 의 log.info { ... } 는 suspend 람다가 아니다)
    //      따라서 한 번 변수로 받아서 출력한다.
    val ctx = coroutineContext
    log.info { "context in suspend: $ctx" }

    val result = suspendCoroutine<Int> { cont ->
        // (2) Continuation 내부 - cont.context 로 접근
        log.info { "context by continuation: ${cont.context}" }
        cont.resume(value = 100)
    }

    log.info { "result: $result" }
}

fun main() {
    runBlocking {
        // (1) CoroutineScope 내부 - this.coroutineContext 로 접근
        //      runBlocking { ... } 안의 this 는 CoroutineScope 이므로 coroutineContext 가 보인다.
        val ctx = this.coroutineContext
        log.info { "context in CoroutineScope: $ctx" }

        child()
    }
}
