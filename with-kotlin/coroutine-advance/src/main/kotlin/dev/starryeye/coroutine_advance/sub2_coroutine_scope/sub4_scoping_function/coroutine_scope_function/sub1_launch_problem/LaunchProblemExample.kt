package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.coroutine_scope_function.sub1_launch_problem

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

/**
 * launch 의 문제
 *
 *
 * 시나리오
 *      runBlocking {
 *          val job = CoroutineScope(EmptyCoroutineContext).launch {
 *              val job1 = launch { delay(100); log.info { "complete job1" } }
 *              val job2 = launch { delay(100); log.info { "complete job2" } }
 *              val job3 = launch { delay(100); log.info { "complete job3" } }
 *
 *              log.info { "step1" }
 *
 *              job1.join()
 *              job2.join()
 *              job3.join()
 *
 *              log.info { "step2" }
 *          }
 *          job.join()
 *      }
 *
 *
 * 문제 의식
 *      - 요구사항: "job1, job2, job3 가 정확히 step1 출력과 step2 출력 사이에 모두 완료되어야 한다."
 *      - 그런데 launch 는 비동기 builder 라 호출 즉시 떨어져 나간다.
 *          - 묶어서 기다리려면 직접 Job 변수를 받아두고
 *          - 각각 .join() 으로 한 줄씩 대기해야 한다.
 *      - 자식이 한두 개일 땐 별 일 아니지만,
 *          - 자식이 많아질수록 Job 을 모으고 일일이 join 하는 코드가 번거롭다.
 *          - 누락되면 그 자식만 비동기로 새어 나간다 (lifecycle leak).
 *          - 만약 for loop 으로 launch 를 N 번 한다면 모든 Job 을 list 에 모아 list.forEach { it.join() } 처리가 필요.
 *      - 핵심 불편함: "structured concurrency 가 코드 단계에서 자동으로 보장되지 않는다."
 *
 *
 * 출력 예시
 *      [DefaultDispatcher-worker-1 @coroutine#2] - step1
 *      [DefaultDispatcher-worker-1 @coroutine#3] - complete job1
 *      [DefaultDispatcher-worker-1 @coroutine#4] - complete job2
 *      [DefaultDispatcher-worker-3 @coroutine#5] - complete job3
 *      [DefaultDispatcher-worker-3 @coroutine#2] - step2
 *
 *      → 결과는 맞아 보이지만, 그건 "맞게 보이려고 join 을 정확히 모두 호출했기 때문" 일 뿐이다.
 *        다음 두 파일에서 (Solution1: launch 로 묶기 → Solution2: coroutineScope) 점진적으로 깔끔해지는 흐름을 본다.
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = EmptyCoroutineContext).launch {
            val job1 = launch {
                delay(timeMillis = 100)
                log.info { "complete job1" }
            }

            val job2 = launch {
                delay(timeMillis = 100)
                log.info { "complete job2" }
            }

            val job3 = launch {
                delay(timeMillis = 100)
                log.info { "complete job3" }
            }

            log.info { "step1" }
            job1.join()
            job2.join()
            job3.join()
            log.info { "step2" }
        }

        job.join()
    }
}
