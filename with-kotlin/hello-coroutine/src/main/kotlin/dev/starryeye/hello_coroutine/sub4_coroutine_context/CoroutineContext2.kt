package dev.starryeye.hello_coroutine.sub4_coroutine_context

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 현재 코루틴의 구조는 runBlocking (루트)코루틴 1개, my-coroutine (루트)코루틴 1개로
 * 총 2개이다.
 *
 * my-coroutine 코루틴은 runBlocking 내부에서 만들어졌지만, CoroutineScope(Dispatchers.IO) 로
 * 완전히 독립된 스코프에서 동작하므로 runBlocking 코루틴의 자식 코루틴이 아니다.
 *
 * CoroutineExceptionHandler 는 루트 코루틴에서만 동작한다.
 * my-coroutine 코루틴은 루트 코루틴 이므로 CoroutineExceptionHandler 가 동작할 수 있다.
 *
 * my-coroutine 코루틴이 동작하면 IllegalStateException 예외가 발생하고 해당 예외는 CoroutineExceptionHandler 가 잡는다.
 */
private val log = KotlinLogging.logger {}

fun main() = runBlocking {

    // CoroutineContext 생성
    val context = CoroutineName("my-coroutine") + // 코루틴 이름
            CoroutineExceptionHandler { _, e -> // 코루틴 예외 핸들러
                log.error(e) { "custom exception handler" }
            }

    CoroutineScope(Dispatchers.IO).launch(context) {
        throw IllegalStateException()
    }

    delay(100)
}