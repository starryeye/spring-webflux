package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub2_main

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.ContinuationInterceptor

/**
 * CoroutineDispatcher Main
 *
 * 핵심
 *      - Dispatchers.Main 은 "플랫폼의 메인 스레드 (Android UI / JavaFX UI / Swing EDT 등)" 를 가리키는 추상이다.
 *      - kotlinx-coroutines-core 자체에는 구현이 없다.
 *          별도 라이브러리(kotlinx-coroutines-android, kotlinx-coroutines-javafx, kotlinx-coroutines-swing 등)
 *          가 ServiceLoader 로 실제 구현을 주입해줘야 동작한다.
 *      - 주입이 없는 환경에서 dispatch 시도하면 IllegalStateException 이 던져진다.
 *
 * - 사실상 백엔드에서는 다룰 일이 없다.
 *
 * Android 의 Main dispatcher (참고)
 *      package kotlinx.coroutines.android
 *
 *      // Dispatches execution onto Android [Handler].
 *      public sealed class HandlerDispatcher :
 *          MainCoroutineDispatcher(), Delay { ... }
 *
 *      Android 메인 스레드의 Looper / Handler 를 사용하는 Main dispatcher 를 제공한다.
 *
 *
 * 출력 예시 (plain JVM, kotlinx-coroutines-core 만 있는 환경)
 *      Exception in thread "main" java.lang.IllegalStateException:
 *          Module with the Main dispatcher is missing.
 *          Add dependency providing the Main dispatcher, e.g. 'kotlinx-coroutines-android'
 *          and ensure it has the same version as 'kotlinx-coroutines-core'
 *
 *
 * 이 예제 동작
 *      - 그냥 Dispatchers.Main 으로 withContext 진입을 시도한다.
 *      - 본 프로젝트는 plain JVM 이라 위 예외가 발생한다.
 *      - 학습용으로 try-catch 로 감싸 메시지만 깔끔하게 찍고 넘어간다.
 */
private val log = KotlinLogging.logger {}

private fun CoroutineScope.dispatcher(): CoroutineDispatcher? =
    this.coroutineContext[ContinuationInterceptor] as? CoroutineDispatcher

fun main() {
    runBlocking {
        try {
            withContext(context = Dispatchers.Main) {
                log.info { "thread: ${Thread.currentThread().name}" }
                log.info { "dispatcher: ${this.dispatcher()}" }
            }
        } catch (e: IllegalStateException) {
            log.error { "Main dispatcher 사용 불가: ${e.message}" }
        }
    }
}
