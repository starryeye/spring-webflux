package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.coroutine_scope.sub2_launch_problem_solve_1

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

/**
 * launch 의 문제 해결 1 — 묶음 launch
 *
 *
 * 아이디어
 *      - 여러 자식 launch 들을 outer launch 로 한 번 더 감싼다.
 *      - outer launch 의 Job (= parentJob) 하나만 .join() 하면, structured concurrency 에 의해
 *          parentJob 은 자기 자식 3개가 모두 끝나야 비로소 완료된다.
 *      - 결과적으로 "자식 3개를 통째로 대기" 효과를 얻는다.
 *
 *
 * 시나리오
 *      runBlocking {
 *          val job = CoroutineScope(EmptyCoroutineContext).launch {
 *              val parentJob = launch {                                    // outer launch — 묶음 역할
 *                  launch { delay(100); log.info { "complete job1" } }  // 자식 1
 *                  launch { delay(100); log.info { "complete job2" } }  // 자식 2
 *                  launch { delay(100); log.info { "complete job3" } }  // 자식 3
 *              }
 *
 *              log.info { "step1" }
 *              parentJob.join()                                            // parentJob 만 join 하면 자식 3개를 통째로 대기
 *              log.info { "step2" }
 *          }
 *
 *          job.join()
 *      }
 *
 *
 * 개선된 점
 *      - 자식이 늘어나도 .join() 을 일일이 늘리지 않아도 됨. parentJob 하나만 join.
 *
 *
 * 한계
 *      - 묶기만 하는 outer launch + 그 Job 을 받아 명시적으로 .join() 호출하는 단계가 여전히 존재.
 *      - outer launch 자체가 비동기 builder 라, 호출자 입장에서 "step1과 step2 사이에 모두 끝나야 한다" 는 의도가
 *          코드 모양에 그대로 드러나지 않는다 (블록 안에 묶여 있어 보이지만 본질은 비동기 흐름).
 *      - 결과값을 받고 싶다면 또 별도로 chain 을 만들어야 한다.
 *
 *
 * 출력 예시
 *      [DefaultDispatcher-worker-1 @coroutine#2] - step1
 *      [DefaultDispatcher-worker-3 @coroutine#4] - complete job1
 *      [DefaultDispatcher-worker-3 @coroutine#5] - complete job2
 *      [DefaultDispatcher-worker-3 @coroutine#6] - complete job3
 *      [DefaultDispatcher-worker-3 @coroutine#2] - step2
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = EmptyCoroutineContext).launch {
            val parentJob = launch {
                launch {
                    delay(timeMillis = 100)
                    log.info { "complete job1" }
                }

                launch {
                    delay(timeMillis = 100)
                    log.info { "complete job2" }
                }

                launch {
                    delay(timeMillis = 100)
                    log.info { "complete job3" }
                }
            }

            log.info { "step1" }
            parentJob.join()
            log.info { "step2" }
        }

        job.join()
    }
}
