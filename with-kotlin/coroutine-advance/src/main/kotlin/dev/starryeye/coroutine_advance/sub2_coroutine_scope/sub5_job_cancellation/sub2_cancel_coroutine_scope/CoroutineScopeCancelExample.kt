package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub5_job_cancellation.sub2_cancel_coroutine_scope

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * cancel - CoroutineScope
 *
 *      - CoroutineScope.cancel() 을 호출하면, scope 의 Job(root) 이 cancel 되고
 *        그 취소가 Job 트리를 따라 아래(자식 coroutine)로 전파되는 모습을 본다.
 *
 *      즉, cancel 되면 그 하위 Job들도 전부 cancel 된다.
 *
 *
 * 흐름
 *      - launch1(=job1) 안에 launch2(=job2), launch3(=job3) 가 delay(1000) 로 대기 중.
 *      - 100ms 뒤 cs.cancel() → scope 의 root Job cancel → 트리를 따라 job1, job2, job3 로 전파.
 *      - 각 자식의 delay 는 suspend 지점이라, 그 자리에서 CancellationException(=JobCancellationException) 이 throw 된다.
 *          → job3 는 try-catch 로 그 예외를 잡아 "I'm cancelled" 를 찍는다.
 *          → job2, job1 은 catch 가 없어 그대로 취소되고 "I'm done" 은 찍히지 않는다.
 *
 *
 *
 * 출력 (대기를 추가한 이 버전)
 *      [DefaultDispatcher-worker-1 @coroutine#3] - job3: I'm cancelled
 *      [DefaultDispatcher-worker-1 @coroutine#3] - e: kotlinx.coroutines.JobCancellationException: ...
 *      [main @coroutine#1] - finished
 *
 *      → job2/job1 의 "I'm done" 은 없다 (취소되어 delay 에서 끊김).
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val cs = CoroutineScope(context = Dispatchers.Default)

        // launch1
        cs.launch {
            // launch2
            launch {
                delay(timeMillis = 1000)
                log.info { "job2: I'm done" }   // 취소되어 도달하지 못함
            }

            // launch3
            launch {
                try {
                    delay(timeMillis = 1000)
                    log.info { "job3: I'm done" }   // 취소되어 도달하지 못함
                } catch (e: Exception) {
                    log.info { "job3: I'm cancelled" }
                    log.info { "e: $e" }            // JobCancellationException
                }
            }

            delay(timeMillis = 1000)
            log.info { "job1: I'm done" }   // 취소되어 도달하지 못함
        }

        delay(timeMillis = 100)
        cs.cancel()   // root Job cancel → 트리 따라 아래로 전파

        delay(timeMillis = 100)
        log.info { "finished" }
    }
}
