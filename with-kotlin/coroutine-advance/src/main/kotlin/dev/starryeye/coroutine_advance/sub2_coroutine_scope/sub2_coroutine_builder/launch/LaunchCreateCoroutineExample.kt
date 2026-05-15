package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub2_coroutine_builder.launch

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

/**
 * launch 예제
 *
 *
 * 시나리오
 *      runBlocking {
 *          val cs = CoroutineScope(EmptyCoroutineContext)        // 텅 빈 context 로 만들어도 Job 은 자동 충전 (sub1_coroutine_scope 참고)
 *          log.info { "job: ${cs.coroutineContext[Job]}" }       // scope 의 Job 출력 (JobImpl{Active}@...)
 *
 *          val job = cs.launch {                                 // coroutine 생성 + start
 *              delay(100)                                        // 비동기로 떨어져 나갔으니 잠깐 쉰다
 *              log.info { "context: ${this.coroutineContext}" }
 *              log.info { "class name: ${this.javaClass.simpleName}" }
 *              log.info { "parent: ${this.coroutineContext[Job]?.parent}" }
 *          }
 *
 *          log.info { "step1" }                                  // launch 가 비동기라 자식이 도는 동안 곧장 찍힘
 *          job.join()                                            // 자식이 끝날 때까지 suspend
 *          log.info { "step2" }
 *      }
 *
 *
 * 포인트
 *      1) cs.coroutineContext[Job] 출력은 JobImpl{Active}@... — 우리가 만든 scope 의 Job.
 *      2) "step1" 이 먼저 찍힌다.
 *          - cs.launch 는 비동기 builder 라 호출 즉시 Job 만 반환하고, main 흐름은 다음 줄로 진행되기 때문.
 *      3) delay(100) 가 풀린 뒤 자식 coroutine 본문이 worker thread 위에서 실행되며 세 줄이 찍힌다.
 *          - this.coroutineContext 안에는 StandaloneCoroutine{Active}@... 이 들어있다.
 *              -> 의미..
 *                 launch 는 StandaloneCoroutine 을 새로 만들고, 이 객체가 곧 자식 coroutine 의 Job 이다.
 *                 launch 인자로 Job 을 넘기지 않았기 때문에 parentContext[Job] 은 scope 의 Job 이고,
 *                 StandaloneCoroutine 은 생성될 때 그 Job 을 parent 로 등록한다.
 *
 *                 구조:
 *                     scope.Job
 *                         └── StandaloneCoroutine(Job)

 *          - class name 은 StandaloneCoroutine. launch 가 만드는 구현체.
 *          - parent 출력 주소가 (1) 에서 출력한 scope.Job 주소와 동일하다.
 *              -> 즉 새 coroutine 의 부모 Job = scope 의 Job. (CoroutineBuilder.kt 의 "보통의 케이스 (a)" 가 그대로 보임)
 *      4) job.join() 으로 자식이 끝날 때까지 main 이 suspend → "step2" 가 마지막에 찍힌다.
 *
 *
 * 출력
 *      [main @coroutine#1]                       - job: JobImpl{Active}@4fb3ee4e
 *      [main @coroutine#1]                       - step1
 *      [DefaultDispatcher-worker-2 @coroutine#2] - context: [CoroutineId(2), "coroutine#2":StandaloneCoroutine{Active}@34e9271e, Dispatchers.Default]
 *      [DefaultDispatcher-worker-2 @coroutine#2] - class name: StandaloneCoroutine
 *      [DefaultDispatcher-worker-2 @coroutine#2] - parent: JobImpl{Active}@4fb3ee4e
 *      [main @coroutine#1]                       - step2
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
        log.info { "job: ${cs.coroutineContext[Job]}" } // CoroutineScope 생성시 EmptyCoroutineContext 를 사용해도 자동으로 Job 이 만들어진다.

        // coroutine 생성됨
        val job = cs.launch {
            // launch 코루틴 빌더로 코루틴을 생성
            //      1) cs.coroutineContext 와 launch 인자로 받은 context 를 merge 해서 새 context 를 만든다.
            //          - 여기서는 launch 에 별도 context 를 넘기지 않았으므로 cs.coroutineContext 가 거의 그대로 사용된다.
            //          - cs.coroutineContext 안에는 CoroutineScope 생성 시 자동으로 붙은 Job 이 들어있다.
            //
            //      2) launch 는 그 context 를 기반으로 StandaloneCoroutine 객체를 새로 만든다.
            //          - StandaloneCoroutine 은 launch 가 만드는 coroutine 구현체다.
            //          - 동시에 Job 구현체이기도 하므로, 이 객체 자체가 "자식 coroutine 의 Job" 이 된다.
            //
            //      3) 새 StandaloneCoroutine 은 생성될 때 parentContext[Job] 을 parent 로 등록한다.
            //          - launch 인자로 Job 을 넘기지 않았으므로 parentContext[Job] == cs.coroutineContext[Job]
            //          - 따라서 구조는 아래와 같다.
            //
            //              cs.coroutineContext[Job]      // 부모 Job, JobImpl
            //                  └── StandaloneCoroutine  // 자식 coroutine 이자 자식 Job
            //
            //      4) launch 는 생성한 StandaloneCoroutine 을 start 하고, 그 Job 을 반환한다.
            //          - 여기서 변수 job 에 담기는 값은 scope 의 Job 이 아니라,
            //            launch 가 새로 만든 자식 coroutine 의 Job 이다.

            delay(timeMillis = 100)
            log.info { "context: ${this.coroutineContext}" }
            log.info { "class name: ${this.javaClass.simpleName}" }
            log.info { "parent: ${this.coroutineContext[Job]?.parent}" }
        }

        log.info { "step1" }
        job.join()
        log.info { "step2" }
    }
}
