package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub5_job_cancellation.cancel.sub5_with_timeout_in_tree

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * withTimeout 예제 - 트리 안에서의 전파
 *
 *      - Job 트리 안에서 한 자식(job2)만 withTimeout 으로 감싸 시간 초과시키면, 형제(job3)·부모(root)·scope 가 어떻게 되는지 본다.
 *      - 결론: withTimeout 초과로 던져지는 TimeoutCancellationException 은 CancellationException 의 하위 타입이라
 *              "정상 취소" 로 취급된다 → 위로 전파되지 않아 형제·부모·scope 가 모두 안전하다.
 *
 *
 * 출력
 *      [...@coroutine#3] - job2: I'm cancelled
 *      [...@coroutine#3] - e2: Timed out waiting for 500 ms
 *      [...@coroutine#4] - job3: I'm done
 *      [main @coroutine#1] - job is cancelled: false
 *      [main @coroutine#1] - csJob is cancelled: false
 *
 *      → job2 만 시간 초과로 취소, job3 는 정상 완료, root·scope 모두 안전(cancelled=false).
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val cs = CoroutineScope(context = Dispatchers.Default)
        val csJob = cs.coroutineContext[Job]

        // launch1 (root)
        val job = cs.launch {
            // launch2 (leaf) — withTimeout 으로 감싼 작업이 시간 초과
            launch {
                withTimeout(timeMillis = 500) {
                    try {
                        delay(timeMillis = 1000)
                        log.info { "job2: I'm done" }
                    } catch (e: Exception) {
                        log.info { "job2: I'm cancelled" }
                        log.info { "e2: ${e.message}" }   // Timed out waiting for 500 ms
                    }
                }
            }

            // launch3 (leaf) — 형제의 timeout 에 영향 없이 끝까지 동작
            launch {
                try {
                    delay(timeMillis = 1000)
                    log.info { "job3: I'm done" }
                } catch (e: Exception) {
                    log.info { "job3: I'm cancelled" }
                    log.info { "e3: ${e.message}" }
                }
            }
        }

        job.join()
        log.info { "job is cancelled: ${job.isCancelled}" }        // false
        log.info { "csJob is cancelled: ${csJob!!.isCancelled}" }  // false
    }
}
