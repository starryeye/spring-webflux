package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub3_thread_local_element

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * CoroutineContext 의 종류 (3-1) - Coroutine 과 ThreadLocal (문제 상황)
 *
 * 문제.
 *      - ThreadLocal 은 "현재 스레드에 묶인 변수" 다.
 *      - Coroutine 은 Dispatcher 에 따라 다른 스레드에서 동작 가능하다.
 *          (suspend/resume 과정에서 다른 worker thread 로 옮겨갈 수 있다)
 *      - 따라서 main 스레드에서 set 한 ThreadLocal 값은
 *          Dispatchers.IO 같은 다른 dispatcher 의 worker 에서는 보이지 않는다.
 *
 *
 */
private val log = KotlinLogging.logger {}

fun main() {
    val greeting = ThreadLocal<String>()
    greeting.set("hello")

    runBlocking {
        // (1) runBlocking 은 호출 스레드(main) 에서 실행 -> 같은 스레드라 ThreadLocal 값 유지
        log.info { "thread: ${Thread.currentThread().name}" }
        log.info { "greeting: ${greeting.get()}" }

        // (2) Dispatchers.IO -> 다른 worker 스레드로 이동 -> 그 스레드의 ThreadLocal 은 비어있다
        launch(Dispatchers.IO) {
            log.info { "thread: ${Thread.currentThread().name}" }
            log.info { "greeting: ${greeting.get()}" }   // null
        }
    }
}
