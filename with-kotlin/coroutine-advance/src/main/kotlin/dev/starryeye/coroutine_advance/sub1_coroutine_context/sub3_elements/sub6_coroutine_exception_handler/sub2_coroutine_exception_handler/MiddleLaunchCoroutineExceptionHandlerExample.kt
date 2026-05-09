package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub6_coroutine_exception_handler.sub2_coroutine_exception_handler

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * CoroutineExceptionHandler 예제 (2) - 중간 launch 에 handler 를 주면 무시된다
 *
 * 시나리오
 *      앞 예제에서는 root launch 의 context 에 handler 가 들어가 있어 정상 동작했다.
 *      이번엔 root 가 아닌 "중간 launch" 의 인자로 handler 를 넘겨본다.
 *
 *      val job = CoroutineScope(Dispatchers.IO).launch {       // 이게 root coroutine
 *          launch(handler) {                                    // 중간 launch (root 아님)
 *              launch { throw ... }
 *          }
 *      }
 *
 * 결과
 *      handler 가 무시되고, exception 이 stderr 에 raw 로 출력된다.
 *
 * 이유
 *      - CoroutineExceptionHandler 는 "root coroutine 의 context 에 있을 때만" 동작한다.
 *      - 중간 launch 에서 발생한 exception 은 어차피 부모로 그대로 전파될 뿐
 *          중간 자식이 들고 있던 handler 는 사용되지 않는다.
 *      - 결국 root 까지 올라갔는데 그쪽 context 에는 handler 가 없으므로
 *          최종적으로 Thread 의 UncaughtExceptionHandler 로 빠져 stderr 에 찍힌다.
 *
 *
 * 출력
 *      Exception in thread "DefaultDispatcher-worker-2 @coroutine#4" java.lang.IllegalStateException: exception in launch
 *          at ...
 *      (note: handler 의 "exception caught" 는 안 찍힘)
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val handler = CoroutineExceptionHandler { _, e ->
            log.error { "exception caught" }   // 안 찍힘
        }

        // root launch 에는 handler 가 없다 (Dispatchers.IO 만)
        val job = CoroutineScope(context = Dispatchers.IO).launch {
            // 중간 launch 에 handler 를 줘봤자 무시된다.
            launch(context = handler) {
                launch {
                    throw IllegalStateException("exception in launch")
                }
            }
        }

        job.join()
    }
}
