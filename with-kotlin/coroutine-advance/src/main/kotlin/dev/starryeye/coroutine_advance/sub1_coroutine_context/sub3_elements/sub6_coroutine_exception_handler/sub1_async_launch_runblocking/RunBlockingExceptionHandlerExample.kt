package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub6_coroutine_exception_handler.sub1_async_launch_runblocking

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.runBlocking

/**
 * Error handling (4) - runBlocking 에 handler 를 줘도 무시된다
 *
 * 시도
 *      runBlocking 의 context 인자로 CoroutineExceptionHandler 를 넣어본다.
 *
 *          runBlocking(handler) {
 *              throw IllegalStateException("exception in runBlocking")
 *          }
 *
 * 결과
 *      handler 는 동작하지 않는다.
 *      대신 exception 이 그대로 caller (= JVM main thread) 로 다시 던져져서
 *      stderr 에 raw 로 출력된다.
 *
 * 이유
 *      - runBlocking 은 다른 builder 와 다르게 "exception 을 caller 에게 그대로 다시 던지는" 모델이다.
 *          (현재 스레드를 막아두고 결과를 기다리는 용도이므로 "함수처럼" 동작)
 *      - 따라서 caller 입장에서 일반 try-catch 로 잡으면 된다 (다음 파일 RunBlockingTryCatchExample 참고).
 *      - launch/async 와 달리 root coroutine 의 handler 가 끼어들 자리가 없다.
 *
 *
 * 출력
 *      Exception in thread "main" java.lang.IllegalStateException: exception in runBlocking
 *          at ...
 *      (note: handler 의 "exception handler" 는 안 찍힘)
 */
private val log = KotlinLogging.logger {}

fun main() {
    val handler = CoroutineExceptionHandler { _, e ->
        log.error { "exception handler" }   // 안 찍힘
    }

    runBlocking(context = handler) {
        throw IllegalStateException("exception in runBlocking")
    }
}
