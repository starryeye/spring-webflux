package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub5_job_cancellation.exception.sub2_supervisor_job

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * exception - SupervisorJob (위로 전파 차단)
 *
 *      - sub1_exception_leaf_coroutine 에서는 leaf 의 예외가 위로 전파되어 트리 전체가 무너졌다.
 *      - 여기서는 그 leaf(job2)를 launch(SupervisorJob()) 으로 띄운다.
 *          → job2 의 부모가 "전파를 위로 올리지 않는" SupervisorJob 이 되어, 예외가 위로 못 올라간다.
 *          → 형제 job3 와 root, scope 가 모두 살아남는다.
 *
 *
 * 핵심
 *      - SupervisorJob 을 context 로 주면, 그 아래에서 난 실패의 cancel 전파가 "아래 방향으로만" 흐른다 (위로 차단).
 *      - 그래서 job2 의 예외는 위로 못 가고, 처리되지 않은 채 CoroutineExceptionHandler(없으면 기본 핸들러)로 빠진다.
 *          → 콘솔에 IllegalStateException 스택트레이스가 찍히지만, 트리는 멀쩡.
 *      - 결과: job3 는 "I'm done", job/csroot 모두 cancelled=false.
 *
 *
 * 주의 (구조적 동시성 관점)
 *      - launch(SupervisorJob()) 은 새 SupervisorJob 을 부모로 달기 때문에, 이 자식은 사실상 cs 트리에서 분리된다.
 *        (job.join() 으로 기다려지는 대상이 아님 → leak 위험) "전파 차단" 개념 시연용으로만 보자.
 *      - 한쪽 "영역" 단위로 자식 실패를 격리하고 싶다면 보통 supervisorScope { } 가 더 안전하다.
 *        → sub4_scoping_function/supervisor_scope 참고.
 *
 *
 * 출력
 *      Exception in thread "..." java.lang.IllegalStateException: unexpected
 *          at ...
 *      [...@coroutine#4] - job3: I'm done
 *      [main @coroutine#1] - job is cancelled: false
 *      [main @coroutine#1] - csJob is cancelled: false
 *
 *      → job2 의 예외는 콘솔에만 찍히고, 형제·root·scope 는 전부 살아남는다 (cancelled=false).
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val cs = CoroutineScope(context = Dispatchers.Default)
        val csJob = cs.coroutineContext[Job]

        // launch1 (root)
        val job = cs.launch {
            // launch2 (leaf) — SupervisorJob 을 부모로 → 예외가 위로 전파되지 않음
            launch(context = SupervisorJob()) {
                delay(timeMillis = 100)
                throw IllegalStateException("unexpected")
            }

            // launch3 (leaf) — 형제의 실패에도 영향 없이 끝까지 동작
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
