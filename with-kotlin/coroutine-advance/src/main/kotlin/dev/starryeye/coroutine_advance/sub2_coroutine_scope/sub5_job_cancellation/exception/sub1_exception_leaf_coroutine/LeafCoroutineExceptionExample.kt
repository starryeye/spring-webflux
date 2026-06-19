package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub5_job_cancellation.exception.sub1_exception_leaf_coroutine

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * exception - Leaf Coroutine
 *
 *      - cancel/sub4_cancel_leaf_coroutine 와 트리 구조는 똑같지만,
 *        leaf 가 cancel 이 아니라 "일반 예외(throw)" 로 끝나면 어떻게 다른지 본다.
 *
 *      - 결론: 일반 예외는 "위 방향(자식 → 부모)" 으로 전파되어 부모(root)와 scope 까지 통째로 cancel 시킨다.
 *
 *
 * 흐름
 *      - job1(root) 안에 job2, job3.
 *      - job2 가 100ms 뒤 IllegalStateException 을 throw → 이 예외가 부모로 전파됨.
 *      - 부모(root)가 cancelling 상태가 되면서 형제 job3 도 cancel → job3 의 delay 에서 JobCancellationException throw.
 *          → job3 의 catch 에서 e3.message 는 "Parent job is Cancelling".
 *      - 결국 root(job) 도, CoroutineScope 의 Job(csJob) 도 모두 cancelled.
 *
 *
 * 위 흐름을 단계로 쪼개면
 *
 *      트리:   CoroutineScope(Job) ─ job1(root) ─┬─ job2 (예외 발생)
 *                                               └─ job3
 *
 *      (1) job2(Coroutine2) 에서 exception 발생 → job2 는 자기 부모(job1)에게 cancel 요청 (위로).
 *      (2) job1(root) 은 "자식이 모두 끝나야" 자기도 정리되므로, 먼저 남은 자식 job3 를 cancel (아래로).
 *      (3) job3 까지 cancel 되면 job1(root) 도 cancelled 상태로. 그 뒤 job1 은 자기 부모(scope 의 Job)에게 cancel 요청 (위로).
 *      (4) scope 의 Job 은 자식이 job1 뿐이므로, 곧바로 scope 의 Job 도 cancel.
 *
 *      → 정리하면 "예외는 위로 한 칸 → 그 부모는 형제들을 아래로 cancel → 다시 위로 한 칸" 을 꼭대기(scope)까지 반복.
 *        그래서 leaf 하나의 예외가 결국 트리 전체를 무너뜨린다.
 *
 *
 * 핵심 (cancel 그룹과의 결정적 차이)
 *      - cancel  : 던지는 게 CancellationException → "정상 취소" 로 취급, 아래 방향으로만 전파.
 *                  → leaf 하나 취소해도 형제·부모 안전 (cancel/sub4_cancel_leaf_coroutine).
 *      - exception: 던지는 게 그 외 일반 예외 → "실패" 로 취급, 위로 전파(자식 → 부모) 후
 *                   부모가 다시 아래(형제)로 CancellationException 을 내려보냄 → 트리 전체가 무너진다 (이 예제).
 *      - 즉 "예외냐 아니냐" 가 아니라 "CancellationException 이냐, 그 외 예외냐" 가 전파 방향을 가른다. (→ JobCancellation.kt)
 *      - 이 "위로 전파" 를 한쪽 가지에서 막고 싶을 때 쓰는 게 supervisorScope / SupervisorJob.
 *        → sub4_scoping_function/supervisor_scope 참고.
 *      - 처리되지 않은 예외 자체의 행선지(CoroutineExceptionHandler)는
 *        → sub1_coroutine_context/.../sub6_coroutine_exception_handler 참고.
 *
 *
 * 출력
 *      [...@coroutine#4] - job3: I'm cancelled
 *      [...@coroutine#4] - e3: Parent job is Cancelling
 *      Exception in thread "..." java.lang.IllegalStateException: unexpected
 *          at ...
 *      [main @coroutine#1] - job is cancelled: true
 *      [main @coroutine#1] - csJob is cancelled: true
 *
 *      → job3 의 취소 사유가 "Parent job is Cancelling" 인 점에 주목 (부모가 먼저 무너져서 내려온 취소).
 *        root 와 scope 의 Job 모두 cancelled=true.
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val cs = CoroutineScope(context = Dispatchers.Default)
        val csJob = cs.coroutineContext[Job]

        // launch1 (root)
        val job = cs.launch {
            // launch2 (leaf) — 예외를 던지는 쪽
            launch {
                delay(timeMillis = 100)
                throw IllegalStateException("unexpected")
            }

            // launch3 (leaf) — 형제의 예외 때문에 덩달아 취소됨
            launch {
                try {
                    delay(timeMillis = 1000)
                    log.info { "job3: I'm done" }
                } catch (e: Exception) {
                    log.info { "job3: I'm cancelled" }
                    log.info { "e3: ${e.message}" }   // Parent job is Cancelling
                }
            }
        }

        job.join()
        log.info { "job is cancelled: ${job.isCancelled}" }        // true
        log.info { "csJob is cancelled: ${csJob!!.isCancelled}" }  // true
    }
}
