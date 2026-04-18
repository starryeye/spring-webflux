package dev.starryeye.coroutine_basic.sub3_continuation_passing_style

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Kotlin 에서의 Continuation
 *
 * Continuation 인터페이스
 *      Kotlin coroutines 에서 Continuation 인터페이스를 제공한다.
 *      package kotlin.coroutines
 *          public interface Continuation<in T> {
 *              public val context: CoroutineContext
 *              public fun resumeWith(result: Result<T>)
 *          }
 *      구성
 *          context : CoroutineContext 를 포함한다.
 *          resumeWith : 외부에서 해당 continuation 을 실행할 수 있는 endpoint 를 제공한다.
 *
 * Continuation 구현
 *      Continuation 인터페이스를 구현하는 익명 클래스를 생성하여, context 와 resumeWith 를 구현한다.
 *      context 에는 EmptyCoroutineContext 를 넣었다.
 *      resumeWith 에서는 상태(visited)에 따라 다른 코드가 실행되도록 했다.
 *      결과(Result.success) 뿐만 아니라 에러(Result.failure)도 전달 가능하다.
 *          resume(value)            -> Result.success(value) 로 resumeWith 호출
 *          resumeWithException(e)   -> Result.failure(e) 로 resumeWith 호출
 */
private val log = KotlinLogging.logger {}

fun main() {
    var visited = false

    val continuation = object : Continuation<Int> {

        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Int>) {
            // 자체 상태(visited)를 가지고, 호출될 때마다 상태에 따라 다른 행동을 한다.
            if (visited) {
                log.info { "Result: $result" }
            } else {
                log.info { "Visit now" }
                visited = true
            }
        }
    }

    continuation.resume(10) // resumeWith(Result.success(10)) 와 동일, visited == false 이므로 "Visit now" 출력
    continuation.resume(10) // visited == true 이므로 "Result: Success(10)" 출력
    continuation.resumeWithException(IllegalStateException()) // 에러도 동일하게 resumeWith 로 전달, "Result: Failure(...)" 출력
}
