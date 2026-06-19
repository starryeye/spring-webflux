package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub5_job_cancellation.sub4_cancel_leaf_coroutine

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * cancel - Leaf Coroutine
 *
 *
 * 무엇을 보나
 *      - 트리 맨 아래의 leaf coroutine 하나(job2)만 cancel 하면 어떻게 되는지 본다.
 *      - 결론: 취소는 "아래 방향" 으로만 흐르므로, 형제(job3)와 부모(root)는 멀쩡하다.
 *
 *
 * 흐름
 *      - job1(root) 안에 job2, job3 가 delay(1000) 로 대기 중.
 *      - 100ms 뒤 job2.cancel() → job2 만 취소 → job2 의 delay 에서 JobCancellationException throw → catch.
 *      - job3 는 영향 없이 1000ms 까지 살아서 "job3: I'm done".
 *      - root(job1) 도 cancel 되지 않는다 → job.isCancelled == false.
 *
 *
 * 출력
 *      [...@coroutine#3] - job2: I'm cancelled
 *      [...@coroutine#3] - e2: StandaloneCoroutine was cancelled
 *      [...@coroutine#4] - job3: I'm done
 *      [main @coroutine#1] - job is cancelled: false
 *
 *      → job2 만 취소, job3 는 정상 완료, root 도 정상(cancelled=false).
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val cs = CoroutineScope(context = Dispatchers.Default)

        // launch1 (root)
        val job = cs.launch {
            // launch2 (leaf) — 이 자식만 취소할 대상
            val job2 = launch {
                try {
                    delay(timeMillis = 1000)
                    log.info { "job2: I'm done" }
                } catch (e: Exception) {
                    log.info { "job2: I'm cancelled" }
                    log.info { "e2: ${e.message}" }
                }
            }

            // launch3 (leaf) — 영향 없이 끝까지 동작
            launch {
                try {
                    delay(timeMillis = 1000)
                    log.info { "job3: I'm done" }
                } catch (e: Exception) {
                    log.info { "job3: I'm cancelled" }
                    log.info { "e3: ${e.message}" }
                }
            }

            delay(timeMillis = 100)
            job2.cancel()   // leaf 하나만 취소
        }

        job.join()
        log.info { "job is cancelled: ${job.isCancelled}" }   // false (root 는 멀쩡)
    }
}
