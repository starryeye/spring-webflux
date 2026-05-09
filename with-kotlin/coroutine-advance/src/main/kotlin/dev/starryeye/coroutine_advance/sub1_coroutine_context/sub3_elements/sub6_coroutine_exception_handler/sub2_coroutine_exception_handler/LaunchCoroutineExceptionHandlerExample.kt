package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub6_coroutine_exception_handler.sub2_coroutine_exception_handler

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * CoroutineExceptionHandler 예제 (1) - root launch 의 context 에 handler 주입
 *
 * 시나리오
 *      - LaunchExceptionExample 에서는 깊은 launch 의 exception 이 stderr 로 raw 출력됐다.
 *      - 이번엔 그 launch 트리의 "root" 에 CoroutineExceptionHandler 를 함께 얹어준다.
 *
 *      val job = CoroutineScope(Dispatchers.IO + handler).launch { ... }   // 이 launch 가 root
 *
 *      깊이 있는 자식 launch 에서 throw 가 발생하면
 *          1) 자식 Job 이 fail
 *          2) cancellation 이 부모 -> 부모 -> root 까지 올라감
 *          3) root coroutine 에서 자기 context 의 handler 를 꺼내 handleException(...) 호출
 *      -> 결과적으로 handler 의 람다가 실행되어 "exception caught" 가 찍힌다.
 *
 * 출력 예시
 *      [DefaultDispatcher-worker-3 @coroutine#4] - exception caught
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        // CoroutineExceptionHandler { ctx, e -> ... } DSL 로 element 생성
        val handler = CoroutineExceptionHandler { _, e ->
            log.error { "exception caught" }
        }

        // root launch 의 context 에 handler 를 주입한다.
        //      Dispatchers.IO + handler -> CombinedContext (Dispatcher + ExceptionHandler)
        val job = CoroutineScope(context = Dispatchers.IO + handler).launch {
            launch {
                launch {
                    throw IllegalStateException("exception in launch")
                }
            }
        }

        job.join()
    }
}
