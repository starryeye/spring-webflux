package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.with_timeout

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.EmptyCoroutineContext

/**
 * withTimeout 예제
 *
 *
 * 목표
 *      withTimeout(500) 으로 제한시간을 걸고, 그 안에 1000ms 짜리 작업을 넣어 일부러 초과시킨다.
 *          - 500ms 시점에 block 이 cancel 되고 TimeoutCancellationException 이 throw 된다.
 *          - 그 예외를 try-catch 로 잡아 "초과했음" 을 처리한다.
 *
 *
 * 협조적 취소 포인트
 *      - block 안의 delay(1000) 가 suspend 지점이라, 500ms 시점에 그 자리에서 협조적으로 끊긴다.
 *      - delay 가 던지는 것은 TimeoutCancellationException → catch 로 잡힌다.
 *
 *
 * 주의
 *      - TimeoutCancellationException 은 CancellationException 의 하위 타입.
 *        여기서는 "withTimeout 호출을 감싼" try-catch 로 잡으므로, 이 예외가 바깥 coroutine 을 취소시키지 않는다.
 *      - 만약 block "안에서" catch (e: Exception) 으로 삼켰다면, 취소 신호를 먹어버려 timeout 이 동작 안 하는 함정이 된다.
 *
 *
 * 출력
 *      [DefaultDispatcher-worker-1 @coroutine#2] - work: start
 *      [DefaultDispatcher-worker-1 @coroutine#2] - timed out: Timed out waiting for 500 ms
 *      [DefaultDispatcher-worker-1 @coroutine#2] - after withTimeout (바깥 coroutine 은 멀쩡)
 *
 *      → "work: done" 은 끝까지 안 찍힌다. 1000ms 작업이 500ms 에 잘렸기 때문.
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = EmptyCoroutineContext).launch {

            try {
                withTimeout(timeMillis = 500) {
                    log.info { "work: start" }
                    delay(timeMillis = 1000)      // 500ms 시점에 여기서 협조적으로 끊긴다
                    log.info { "work: done" }      // 도달하지 못함
                }
            } catch (e: TimeoutCancellationException) {
                log.info { "timed out: ${e.message}" }
            }

            log.info { "after withTimeout (바깥 coroutine 은 멀쩡)" }
        }

        job.join()
    }
}
