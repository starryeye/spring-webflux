package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub2_coroutine_builder.async

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

/**
 * async 예제
 *
 *
 * 시나리오
 *      runBlocking {
 *          val cs = CoroutineScope(EmptyCoroutineContext)        // 텅 빈 context 로 만들어도 Job 은 자동 충전
 *          log.info { "job: ${cs.coroutineContext[Job]}" }       // scope 의 Job 출력 (JobImpl{Active}@...)
 *
 *          val deferred = cs.async {                             // coroutine 생성 + start, 결과 타입 T 를 갖는 Deferred 반환
 *              delay(100)                                        // 비동기로 떨어져 나갔으니 잠깐 쉰다
 *              log.info { "context: ${this.coroutineContext}" }
 *              log.info { "class name: ${this.javaClass.simpleName}" }
 *              log.info { "parent: ${this.coroutineContext[Job]?.parent}" }
 *
 *              100                                               // block 의 반환값. await() 으로 받을 수 있다.
 *          }
 *
 *          log.info { "step1" }                                  // async 는 비동기라 자식이 도는 동안 곧장 찍힘
 *          log.info { "result: ${deferred.await()}" }            // 자식이 끝날 때까지 suspend, 끝나면 100 을 받아옴
 *          log.info { "step2" }
 *      }
 *
 *
 * 포인트
 *      1) cs.coroutineContext[Job] 출력은 JobImpl{Active}@... — 우리가 만든 scope 의 Job.
 *      2) "step1" 이 먼저 찍힌다.
 *          - cs.async 는 비동기 builder 라 호출 즉시 Deferred 만 반환하고, main 흐름은 다음 줄로 진행되기 때문.
 *      3) delay(100) 가 풀린 뒤 자식 coroutine 본문이 worker thread 위에서 실행되며 세 줄이 찍힌다.
 *          - this.coroutineContext 안에는 DeferredCoroutine{Active}@... 이 들어있다.
 *              -> 자식 coroutine 자기 자신이 Job element 자리를 차지. (parentContext + this)
 *          - class name 은 DeferredCoroutine. async 가 만드는 구현체.
 *          - parent 출력 주소가 (1) 에서 출력한 scope.Job 주소와 동일하다.
 *              -> 즉 새 coroutine 의 부모 Job = scope 의 Job. (launch 예제 때와 같은 결과)
 *      4) deferred.await() 으로 자식이 끝날 때까지 suspend, 반환값 100 을 받아 "result: 100" 출력.
 *      5) 그 뒤 "step2" 가 마지막에 찍힌다.
 *
 *      launch 와의 비교
 *          - 같은 흐름이지만 builder 의 반환이 Job(launch) vs Deferred<T>(async) 라는 점이 다르다.
 *          - 그래서 launch 는 .join() 으로만 대기 (값 없음), async 는 .await() 으로 대기 + 값 수신 가능.
 *
 *
 * 출력 예시
 *      [main @coroutine#1]                       - job: JobImpl{Active}@4fb3ee4e
 *      [main @coroutine#1]                       - step1
 *      [DefaultDispatcher-worker-1 @coroutine#2] - context: [CoroutineId(2), "coroutine#2":DeferredCoroutine{Active}@5a11e594, Dispatchers.Default]
 *      [DefaultDispatcher-worker-1 @coroutine#2] - class name: DeferredCoroutine
 *      [DefaultDispatcher-worker-1 @coroutine#2] - parent: JobImpl{Active}@4fb3ee4e
 *      [main @coroutine#1]                       - result: 100
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
        log.info { "job: ${cs.coroutineContext[Job]}" }

        val deferred = cs.async {
            // coroutine 생성됨
            delay(timeMillis = 100)
            log.info { "context: ${this.coroutineContext}" }
            log.info { "class name: ${this.javaClass.simpleName}" }
            log.info { "parent: ${this.coroutineContext[Job]?.parent}" }

            100
        }

        log.info { "step1" }
        val result = deferred.await()
        log.info { "result: $result" }
        log.info { "step2" }
    }
}
