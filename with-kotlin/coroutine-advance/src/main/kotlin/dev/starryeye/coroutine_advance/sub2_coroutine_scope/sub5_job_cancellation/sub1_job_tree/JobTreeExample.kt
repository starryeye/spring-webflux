package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub5_job_cancellation.sub1_job_tree

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 *
 * 잠깐 복습.
 *
 * Job
 *      Job은 Start 할 수 도 있고 Cancel 할 수 도 있는 단위이다.
 *      Job은 자식으로 포함되어있는 Job 들의 생명 주기를 관리한다.
 *
 * Job 트리
 *      CoroutineScope 와 그 안에서 만들어지는 coroutine 들이 Job 을 매개로 어떻게 부모-자식 트리를 이루는지 본다.
 *
 *
 *
 * 출력 예시
 *      [main @coroutine#1] - job1: JobImpl{Active}@...
 *      [main @coroutine#1] - parent1: null
 *      [...worker-1 @coroutine#2] - job2: "coroutine#2":StandaloneCoroutine{Active}@...
 *      [...worker-1 @coroutine#2] - parent2: JobImpl{Active}@...                         (= cs 의 Job)
 *      [...worker-2 @coroutine#3] - job3: "coroutine#3":StandaloneCoroutine{Active}@...
 *      [...worker-2 @coroutine#3] - parent3: "coroutine#2":StandaloneCoroutine{Active}@... (= job2)
 *      [...worker-3 @coroutine#4] - job4: "coroutine#4":StandaloneCoroutine{Active}@...
 *      [...worker-3 @coroutine#4] - parent4: "coroutine#2":StandaloneCoroutine{Active}@... (= job2)
 *
 *      → cs(JobImpl) ─ job2 ─┬─ job3
 *                            └─ job4   구조.
 *        (worker 번호·@... 해시는 실행마다 다를 수 있고, job2 의 상태는 Active/Completing 으로 보일 수 있음.
 *         핵심은 parent3·parent4 의 @... 해시가 job2 의 것과 같다는 점)
 */
private val log = KotlinLogging.logger {}

@OptIn(ExperimentalCoroutinesApi::class)   // Job.parent 가 @ExperimentalCoroutinesApi 라서 opt-in 필요
fun main() {
    runBlocking {
        val cs = CoroutineScope(context = Dispatchers.Default)
        log.info { "job1: ${cs.coroutineContext[Job]}" }
        log.info { "parent1: ${cs.coroutineContext[Job]!!.parent}" }   // null (트리의 뿌리)

        val job2 = cs.launch {
            log.info { "job2: ${coroutineContext[Job]}" }
            log.info { "parent2: ${coroutineContext[Job]!!.parent}" }  // cs 의 JobImpl

            launch {
                log.info { "job3: ${coroutineContext[Job]}" }
                log.info { "parent3: ${coroutineContext[Job]!!.parent}" }  // job2
            }

            launch {
                log.info { "job4: ${coroutineContext[Job]}" }
                log.info { "parent4: ${coroutineContext[Job]!!.parent}" }  // job2
            }
        }

        // CoroutineScope 자체는 join 이 없으므로, 자식들이 다 찍을 때까지 job2 를 join 해서 출력을 확정한다.
        job2.join()
    }
}
