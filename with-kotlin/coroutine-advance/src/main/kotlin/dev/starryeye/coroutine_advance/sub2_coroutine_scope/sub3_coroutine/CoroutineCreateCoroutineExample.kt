package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub3_coroutine

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Coroutine 예제
 *
 *
 * 포인트
 *      1) cs.launch { ... } 로 coroutine1 이 만들어진다.
 *          - 본문 안의 this 가 곧 coroutine1 인스턴스.
 *          - Coroutine 은 CoroutineScope 을 구현한다.
 *                  그래서, this 는 CoroutineScope 이다.
 *          - this.coroutineContext[Job] 출력은 coroutine1 자기 자신. (Coroutine 은 Job 구현하기 때문)
 *                  this == this.coroutineContext[Job]
 *
 *      2) coroutine1 본문 안에서 this.launch { ... } 로 coroutine2 를 생성할 수 있다.
 *          - 이것이 가능한 이유는 정확히 "coroutine1 이 CoroutineScope 이라서".
 *          - 만약 Coroutine 이 Job 이기만 하고 CoroutineScope 이 아니었다면 본문 안에서 launch / async 호출이 안 됐을 것.
 *
 *      3) coroutine2 의 parent job 주소가 coroutine1 의 Job 주소와 정확히 같다.
 *          - Coroutine = Job 이므로 "부모 Job" 이라는 표현이 곧 "부모 Coroutine" 을 가리킨다.
 *          - 그래서 Job 트리가 곧 Coroutine 트리와 동치.
 *
 *      4) 출력 순서로 비동기 + structured concurrency 확인
 *          - "step1" 이 먼저 찍힘 → cs.launch 가 비동기 builder 라 호출 즉시 다음 줄로 진행.
 *          - 그 사이에 자식 coroutine1, coroutine2 가 worker thread 위에서 차례로 진행.
 *          - coroutine1 은 자식 coroutine2 가 끝나야 비로소 완료 (structured concurrency).
 *          - main 은 job.join() 으로 coroutine1 이 끝날 때까지 suspend → "step2" 가 가장 나중에 찍힘.
 *
 *
 * 출력
 *      [main @coroutine#1]                       - step1
 *      [DefaultDispatcher-worker-1 @coroutine#2] - job: "coroutine#2":StandaloneCoroutine{Active}@48be7984
 *      [DefaultDispatcher-worker-1 @coroutine#3] - parent job: "coroutine#2":StandaloneCoroutine{Active}@48be7984
 *      [DefaultDispatcher-worker-1 @coroutine#3] - coroutine2 finished
 *      [DefaultDispatcher-worker-1 @coroutine#2] - coroutine1 finished
 *      [main @coroutine#1]                       - step2
 *
 *      → coroutine2 의 parent job 주소 (@48be7984) == coroutine1 의 job 주소.
 *
 *
 * @OptIn(ExperimentalCoroutinesApi::class) 가 필요한 이유
 *      - Job.parent 프로퍼티가 @ExperimentalCoroutinesApi 로 마킹되어 있어 opt-in 이 필요하다.
 */
private val log = KotlinLogging.logger {}

@OptIn(ExperimentalCoroutinesApi::class)
fun main() {
    runBlocking {
        val cs = CoroutineScope(context = EmptyCoroutineContext)
        log.info { "cs: $cs" }

        val job = cs.launch {
            // coroutine1 생성됨
            delay(timeMillis = 100)
            log.info { "job1: ${this.coroutineContext[Job]} (== this: [$this])" }
            log.info { "parent of job1: ${this.coroutineContext[Job]?.parent}" }

            val job2 = this.launch { // 코루틴으로 launch 를 이용할 수 있는 이유가 Coroutine 자체가 CoroutineScope 라서 그렇다.
                // coroutine2 생성됨

                delay(timeMillis = 500)
                log.info { "job2: ${this.coroutineContext[Job]}" }
                log.info { "parent of job2: ${this.coroutineContext[Job]?.parent}" }
                log.info { "coroutine2 finished" }
            }

            job2.join()
            log.info { "coroutine1 finished" }
        }

        log.info { "step1" }
        job.join()
        log.info { "step2" }
    }
}
