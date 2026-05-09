package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub6_coroutine_exception_handler.sub1_async_launch_runblocking

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

/**
 * Error handling (5) - runBlocking 외부에서 try-catch 로 잡을 수 있다
 *
 * 핵심
 *      runBlocking 은 "현재 스레드를 막고 람다를 동기적으로 실행" 하는 builder 다.
 *      그래서 람다 안에서 발생한 exception 은 caller 로 그대로 다시 throw 된다.
 *      -> 일반 함수 호출처럼 try-catch 로 감싸서 잡을 수 있다.
 *
 *      앞 예제 (RunBlockingExceptionHandlerExample) 에서는 handler 가 무시됐지만,
 *      try-catch 로는 정상적으로 잡힌다는 점이 핵심.
 *
 *      다음 sub2_coroutine_exception_handler 에서 launch 트리의 root 에 handler 를 붙이는 방법을 다룬다.
 *
 *
 * 출력
 *      [main] - exception caught
 */
private val log = KotlinLogging.logger {}

fun main() {
    try {
        runBlocking {
            throw IllegalStateException("exception in runBlocking")
        }
    } catch (e: IllegalStateException) {
        log.error { "exception caught" }
    }
}
