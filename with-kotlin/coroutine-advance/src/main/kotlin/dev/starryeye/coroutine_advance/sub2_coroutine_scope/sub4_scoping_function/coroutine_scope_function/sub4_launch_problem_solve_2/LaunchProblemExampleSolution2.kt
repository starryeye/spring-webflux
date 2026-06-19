package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.coroutine_scope_function.sub4_launch_problem_solve_2

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

/**
 * launch 의 문제 해결 2 — coroutineScope
 *
 *
 * 아이디어
 *      - step1 과 step2 사이에 coroutineScope { ... } 를 끼워 넣는다.
 *      - coroutineScope 는 suspend 함수라 caller 입장에선 "block 이 끝날 때까지 그 자리에서 suspend 후 resume" — 즉 동기적.
 *      - 그 block 안에서 띄운 launch 들은 모두 coroutineScope 가 만든 ScopeCoroutine 의 Job 을 부모로 매달린다.
 *          → 자식이 다 끝나야 coroutineScope 가 끝나도록 structured concurrency 가 자동 보장된다.
 *
 *
 * Solution 1 대비 깔끔해진 점
 *      - 묶기만 하는 outer launch + job1.join() 패턴이 사라짐.
 *      - "step1과 step2 사이에 모두 끝나야 한다" 는 의도가 코드 모양 (블록의 구간) 으로 그대로 드러난다.
 *      - 자식 launch 들이 모두 완료되어야 coroutineScope 가 끝남을 ScopeCoroutine 의 Job 계층이 자동 보장.
 *          → 명시적 join 호출 / Job 수집이 모두 사라짐.
 *      - block 의 마지막 표현 값을 그대로 받아 쓸 수도 있다 (다음 파일 LaunchAndCoroutineScopeExample.kt 참고).
 *
 *
 * 출력
 *      [DefaultDispatcher-worker-1 @coroutine#2] - step1
 *      [DefaultDispatcher-worker-3 @coroutine#4] - complete job2
 *      [DefaultDispatcher-worker-4 @coroutine#5] - complete job3
 *      [DefaultDispatcher-worker-2 @coroutine#3] - complete job1
 *      [DefaultDispatcher-worker-2 @coroutine#2] - step2
 *
 *      → 자식 3개가 모두 step1 과 step2 사이에 끝남이 보장된다.
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = EmptyCoroutineContext).launch {

            log.info { "step1" }
            coroutineScope {
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
            log.info { "step2" }
        }

        job.join()
    }
}
