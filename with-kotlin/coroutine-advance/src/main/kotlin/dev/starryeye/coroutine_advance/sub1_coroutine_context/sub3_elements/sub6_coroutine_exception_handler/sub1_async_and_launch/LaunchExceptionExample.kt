package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub6_coroutine_exception_handler.sub1_async_and_launch

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Error handling (2) - launch 의 exception handling
 *
 * async 와의 차이
 *      - async 는 exception 을 Deferred 안에 보관 -> await() 에서 다시 던진다 -> try-catch 가능.
 *      - launch 는 보관해두지 않는다. 발생한 exception 은
 *          1) 자기 Job 을 fail 시키고
 *          2) 부모 Job 으로 "cancellation 형태로" 전파된다.
 *          3) 어디에서도 catch 되지 못하면 Thread 의 UncaughtExceptionHandler 로 빠져 stderr 에 찍힌다.
 *      -> 그래서 job.join() 을 try-catch 로 감싸도 caught 되지 않고, 콘솔에는 raw exception 이 그대로 보인다.
 *
 *      ("exception 이 처리되지 못하고 Thread 의 UncaughtExceptionHandler 를 통해 출력")
 *
 *
 * 예제
 *      - CoroutineScope(Dispatchers.IO).launch 안에서 깊이 중첩된 launch 가 throw
 *      - runBlocking 측에서 try { job.join() } catch { ... } 로 감싸도 catch 가 동작하지 않음
 *      - 결국 stderr 에 "Exception in thread DefaultDispatcher-worker-X @coroutine#N ..." 형태로 raw 출력
 *
 * 출력
 *      Exception in thread "DefaultDispatcher-worker-2 @coroutine#4" java.lang.IllegalStateException: exception in launch
 *          at ...
 *
 *      (note: "exception caught maybe" 는 절대 찍히지 않는다)
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = Dispatchers.IO).launch {
            launch {
                launch {
                    throw IllegalStateException("exception in launch")
                }
            }
        }

        // 시도는 해보지만, launch 의 exception 은 join() 으로 잡히지 않는다.
        //      자식 -> 부모로 "cancellation" 형태로 전파될 뿐이라
        //      job.join() 은 그저 "끝났음" 만 알려주고 throw 하지 않는다.
        try {
            job.join()
        } catch (e: Exception) {
            log.error { "exception caught maybe" }   // 안 찍힘
        }
    }
}
