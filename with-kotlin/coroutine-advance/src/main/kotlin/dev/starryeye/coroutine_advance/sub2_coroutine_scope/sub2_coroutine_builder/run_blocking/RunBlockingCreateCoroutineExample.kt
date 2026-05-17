package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub2_coroutine_builder.run_blocking

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * runBlocking 예제
 *
 *
 * 시나리오
 *      fun main() {
 *          log.info { "before runBlocking - thread: ${Thread.currentThread().name}" }
 *
 *          val result = runBlocking {                                // caller (main) thread 를 block 하고 본문 진입
 *              log.info { "inside  runBlocking - thread: ${Thread.currentThread().name}" }
 *              log.info { "inside  runBlocking - context: ${this.coroutineContext}" }
 *              log.info { "inside  runBlocking - class name: ${this.javaClass.simpleName}" }
 *              log.info { "inside  runBlocking - parent: ${this.coroutineContext[Job]?.parent}" }
 *
 *              launch {                                              // 자식 coroutine
 *                  delay(100)
 *                  log.info { "child   launch       - thread: ${Thread.currentThread().name}" }
 *                  log.info { "child   launch       - parent: ${this.coroutineContext[Job]?.parent}" }
 *              }
 *
 *              "done"                                                // block 의 반환값. runBlocking 이 그대로 돌려준다.
 *          }
 *
 *          log.info { "after  runBlocking - thread: ${Thread.currentThread().name}, result: $result" }
 *      }
 *
 *
 * 포인트
 *      1) runBlocking 본문 안에서 Thread.currentThread().name 이 보통 "main" 으로 찍힌다.
 *          - dispatcher 를 명시하지 않았으니 BlockingEventLoop 이 caller (main) thread 위에서 돌아가기 때문.
 *          - launch / async 의 경우 worker-N 으로 찍히던 것과 대비된다.
 *      2) class name 은 BlockingCoroutine — runBlocking 이 만드는 구현체.
 *      3) parent 는 null.
 *          - runBlocking 은 외부 scope 없이 진입점에서 만들어지므로 부모 Job 이 없다 (RunBlocking.kt 의 "부모-자식 관계" 참고).
 *      4) 안에서 launch 한 자식 coroutine 의 parent 는 BlockingCoroutine 자신의 Job 주소.
 *          - 즉 runBlocking 본문이 곧 자식들의 부모. 부모-자식 관계가 그대로 만들어진다.
 *      5) launch 가 끝날 때까지 runBlocking 은 안 끝난다.
 *          - structured concurrency: 자식이 다 끝나야 부모도 완료.
 *          - 그래서 "after runBlocking" 줄은 자식 launch 의 출력이 모두 끝난 뒤에 찍힌다.
 *      6) runBlocking 이 끝난 뒤 thread 는 여전히 main. caller thread 를 block 했다가 풀어준 것이지, 별도 thread 를 쓴 게 아니다.
 *
 *
 * 출력
 *      [main]              - before runBlocking - thread: main
 *      [main @coroutine#1] - inside  runBlocking - thread: main @coroutine#1
 *      [main @coroutine#1] - inside  runBlocking - context: [...BlockingCoroutine{Active}@..., BlockingEventLoop@...]
 *      [main @coroutine#1] - inside  runBlocking - class name: BlockingCoroutine
 *      [main @coroutine#1] - inside  runBlocking - parent: null
 *      [main @coroutine#2] - child   launch       - thread: main @coroutine#2
 *      [main @coroutine#2] - child   launch       - parent: BlockingCoroutine{Completing}@...
 *      [main]              - after  runBlocking - thread: main, result: done
 *
 *
 * @OptIn(ExperimentalCoroutinesApi::class) 가 필요한 이유
 *      - Job.parent 프로퍼티가 @ExperimentalCoroutinesApi 로 마킹되어 있어 opt-in 이 필요하다.
 */
private val log = KotlinLogging.logger {}

@OptIn(ExperimentalCoroutinesApi::class)
fun main() {
    log.info { "before runBlocking - thread: ${Thread.currentThread().name}" }

    val result = runBlocking {
        log.info { "inside  runBlocking - thread: ${Thread.currentThread().name}" }
        log.info { "inside  runBlocking - context: ${this.coroutineContext}" }
        log.info { "inside  runBlocking - class name: ${this.javaClass.simpleName}" }
        log.info { "inside  runBlocking - parent: ${this.coroutineContext[Job]?.parent}" }

        launch {
            delay(timeMillis = 100)
            log.info { "child   launch       - thread: ${Thread.currentThread().name}" }
            log.info { "child   launch       - parent: ${this.coroutineContext[Job]?.parent}" }
        }

        "done"
    }

    log.info { "after  runBlocking - thread: ${Thread.currentThread().name}, result: $result" }
}
