package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub3_io_and_default.dispatcher_inheritance_cases

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.ContinuationInterceptor

/**
 * Case 2
 *      부모 coroutine: runBlocking (dispatcher = Dispatchers.IO)
 *      launch 인자 dispatcher: Dispatchers.Default
 *      결과: launch 인자 dispatcher (Default) 가 우선.
 *
 * 관찰 포인트
 *      - foldCopies(parent, arg) 에서 arg 쪽이 같은 key (ContinuationInterceptor) 의 값을 덮어쓴다.
 *        그래서 자식이 사용할 dispatcher 는 인자에 준 Default 다.
 *
 * 출력 예시
 *      [parent] thread: DefaultDispatcher-worker-1, dispatcher: Dispatchers.IO
 *      [child]  thread: DefaultDispatcher-worker-2, dispatcher: Dispatchers.Default
 */
private val log = KotlinLogging.logger {}

private fun CoroutineScope.dispatcher(): CoroutineDispatcher? =
    this.coroutineContext[ContinuationInterceptor] as? CoroutineDispatcher

fun main() {
    runBlocking(context = Dispatchers.IO) {
        // 부모 coroutine: runBlocking. dispatcher = IO.
        log.info { "[parent] thread: ${Thread.currentThread().name}, dispatcher: ${this.dispatcher()}" }

        launch(context = Dispatchers.Default) {
            // 자식 coroutine: 인자 Default → 인자 우선.
            log.info { "[child]  thread: ${Thread.currentThread().name}, dispatcher: ${this.dispatcher()}" }
        }
    }
}
