package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub5_job_cancellation.sub3_cancel_root_coroutine

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * cancel - Root Coroutine
 *
 *      - root coroutine(job1) 을 cancel 하면, 그 아래 자식들(job2, job3)까지 취소가 전파되는 모습.
 *      - 앞 예제(sub2)와 달리 여기서는 scope 가 아니라 "root coroutine 자체" 를 cancel 한다.
 *      - 그리고 cancelAndJoin 으로 "취소하고 끝날 때까지 대기" 하므로, 자식들의 취소 결과를 눈으로 확인할 수 있다.
 *          → CoroutineScope 와 달리 Job 은 join 을 제공한다는 점이 핵심.
 *
 *
 * 흐름
 *      - job1 안에 job2, job3 가 delay(1000) 로 대기 중.
 *      - 100ms 뒤 job1.cancelAndJoin() → job1 cancel → 트리를 따라 job2, job3 로 전파.
 *      - 각 자식의 delay 에서 JobCancellationException 이 throw 되어 catch 로 잡힌다.
 *      - job1 자신의 delay(1000) 도 취소되어 "job1: I'm done" 은 찍히지 않는다.
 *
 *
 * 핵심
 *      - 취소는 트리를 따라 "아래 방향(부모 → 자식)" 으로 전파된다.
 *      - cancelAndJoin = cancel() 후 join() 을 묶은 것. "취소를 요청하고, 다 정리될 때까지 기다림".
 *
 *
 * 출력 (자식 순서는 실행마다 바뀔 수 있음)
 *      [...@coroutine#4] - job3: I'm cancelled
 *      [...@coroutine#4] - e3: StandaloneCoroutine was cancelled
 *      [...@coroutine#3] - job2: I'm cancelled
 *      [...@coroutine#3] - e2: StandaloneCoroutine was cancelled
 *
 *      → "I'm done" 은 셋 다 찍히지 않는다.
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val cs = CoroutineScope(context = Dispatchers.Default)

        // launch1
        val job1 = cs.launch {
            // launch2
            launch {
                try {
                    delay(timeMillis = 1000)
                    log.info { "job2: I'm done" }
                } catch (e: Exception) {
                    log.info { "job2: I'm cancelled" }
                    log.info { "e2: ${e.message}" }
                }
            }

            // launch3
            launch {
                try {
                    delay(timeMillis = 1000)
                    log.info { "job3: I'm done" }
                } catch (e: Exception) {
                    log.info { "job3: I'm cancelled" }
                    log.info { "e3: ${e.message}" }
                }
            }

            delay(timeMillis = 1000)
            log.info { "job1: I'm done" }   // 취소되어 도달하지 못함
        }

        delay(timeMillis = 100)
        job1.cancelAndJoin()   // root coroutine 을 취소하고, 다 정리될 때까지 대기
    }
}
