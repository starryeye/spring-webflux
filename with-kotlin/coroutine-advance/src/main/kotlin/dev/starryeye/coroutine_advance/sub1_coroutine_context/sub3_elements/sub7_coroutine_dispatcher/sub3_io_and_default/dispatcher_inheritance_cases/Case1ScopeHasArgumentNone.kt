package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub3_io_and_default.dispatcher_inheritance_cases

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.ContinuationInterceptor

/**
 * Case 1
 *      부모 coroutine: runBlocking (dispatcher = Dispatchers.IO)
 *      launch 인자 dispatcher: 없음
 *      결과: 부모의 IO 를 그대로 상속.
 *
 * 관찰 포인트
 *      - runBlocking 에 Dispatchers.IO 를 주면 runBlocking 자체가 IO 위에서 실행되는 부모 coroutine 이 된다
 *        (기본 BlockingEventLoop 대신 IO 가 들어감).
 *      - 그 안의 launch 는 인자 dispatcher 가 없으므로 부모 context 의 IO 를 상속받아 IO 에서 실행.
 *      - newCoroutineContext 의 Default fallback 분기는 타지 않는다 (이미 dispatcher 가 있으므로).
 *
 * 출력 예시
 *      [parent] thread: DefaultDispatcher-worker-1, dispatcher: Dispatchers.IO
 *      [child]  thread: DefaultDispatcher-worker-2, dispatcher: Dispatchers.IO
 */
private val log = KotlinLogging.logger {}

private fun CoroutineScope.dispatcher(): CoroutineDispatcher? =
    this.coroutineContext[ContinuationInterceptor] as? CoroutineDispatcher

fun main() {
    runBlocking(context = Dispatchers.IO) {
        // 부모 coroutine: runBlocking 자신. dispatcher = IO.
        log.info { "[parent] thread: ${Thread.currentThread().name}, dispatcher: ${this.dispatcher()}" }

        launch {
            // 자식 coroutine: launch 인자 dispatcher 없음 → 부모(IO) 상속.
            log.info { "[child]  thread: ${Thread.currentThread().name}, dispatcher: ${this.dispatcher()}" }
        }
    }
}
