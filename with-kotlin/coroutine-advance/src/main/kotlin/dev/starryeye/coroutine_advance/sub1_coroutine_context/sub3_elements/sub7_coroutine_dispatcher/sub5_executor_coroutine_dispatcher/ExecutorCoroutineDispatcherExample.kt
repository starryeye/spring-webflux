package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub5_executor_coroutine_dispatcher

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * ExecutorCoroutineDispatcher 사용 예제
 *
 * 시나리오
 *      val single = newSingleThreadContext("single")          // 1-thread dispatcher
 *      val fixed  = newFixedThreadPoolContext(4, "fixed")     // 4-thread dispatcher
 *
 *      runBlocking {
 *          launch(single) {
 *              [thread1]                                      // single 의 단일 thread "single"
 *              withContext(fixed) {
 *                  [thread2]                                  // fixed 풀의 thread 중 하나 (예: "fixed-1")
 *                  withContext(Dispatchers.IO) {
 *                      [thread3]                              // IO/Default 공용 worker (DefaultDispatcher-worker-N)
 *                      withContext(single) {
 *                          [thread4]                          // 다시 single thread
 *                      }
 *                  }
 *              }
 *          }.join()
 *
 *          single.close()
 *          fixed.close()
 *      }
 *
 *
 * 포인트
 *      1) launch(single) 로 시작하면 coroutine 본문이 single dispatcher 의 단일 thread "single" 위에서 실행 (thread1).
 *      2) withContext(fixed) 로 들어가면 fixed 풀의 thread 로 dispatch 된다 (thread2).
 *          - thread name 이 "fixed-1", "fixed-2" 식으로 붙는 이유는 newFixedThreadPoolContext 내부에서
 *            nThreads != 1 일 때 "name-번호" 로 thread 이름을 짓기 때문.
 *      3) withContext(Dispatchers.IO) 는 IO/Default 공용 worker pool 의 thread 로 이동한다 (thread3).
 *      4) withContext(single) 로 다시 들어가면 single dispatcher 의 "single" thread 로 돌아온다 (thread4).
 *          - single 은 thread 하나뿐이라 동일한 thread 이름이 나온다.
 *      5) 본문이 끝난 뒤에는 single.close() / fixed.close() 로 내부 Executor 를 종료해야 한다.
 *          - 안 하면 daemon thread 들이 살아 있는 채로 자원을 잡고 있게 된다.
 *
 *
 * 출력
 *      [single @coroutine#2]                       - thread1: single @coroutine#2
 *      [fixed-1 @coroutine#2]                      - thread2: fixed-1 @coroutine#2
 *      [DefaultDispatcher-worker-1 @coroutine#2]   - thread3: DefaultDispatcher-worker-1 @coroutine#2
 *      [single @coroutine#2]                       - thread4: single @coroutine#2
 *
 *
 * @OptIn(DelicateCoroutinesApi::class) 가 필요한 이유
 *      - newSingleThreadContext / newFixedThreadPoolContext 가 @DelicateCoroutinesApi 로 마킹돼 있어,
 *        opt-in 을 명시해 "Executor lifecycle (close) 까지 내가 책임진다" 는 의사를 표시해야 컴파일된다.
 */
private val log = KotlinLogging.logger {}

private fun threadName(): String = Thread.currentThread().name

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    runBlocking {
        val single = newSingleThreadContext(name = "single")
        val fixed = newFixedThreadPoolContext(nThreads = 4, name = "fixed")

        val job = launch(context = single) {
            log.info { "thread1: ${threadName()}" }

            withContext(context = fixed) {
                log.info { "thread2: ${threadName()}" }

                withContext(context = Dispatchers.IO) {
                    log.info { "thread3: ${threadName()}" }

                    withContext(context = single) {
                        log.info { "thread4: ${threadName()}" }
                    }
                }
            }
        }

        job.join()
        single.close()
        fixed.close()
    }
}
