package dev.starryeye.hello_coroutine.sub4_coroutine_context

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

/**
 * CoroutineContext 를 통해서 coroutine 에 설정이 가능하다.
 */
private val log = KotlinLogging.logger {}

val threadLocal = ThreadLocal<String>()

fun main() = runBlocking {

    threadLocal.set("hello")

    log.info { "thread: ${Thread.currentThread().name}" }
    log.info { "threadLocal: ${threadLocal.get()}" }

    // CoroutineContext 생성
    val context: CoroutineContext = CoroutineName("my-coroutine-name") + // 코루틴 이름
            Dispatchers.IO + // 어느 쓰레드에서 동작하게 할 것인지
            threadLocal.asContextElement() // threadLocal 값을 CoroutineContext 에 실어놓으면 추후 어느 스레드에서 재개되든 값이 따라간다.

    launch(context) {
        log.info { "thread: ${Thread.currentThread().name}" }
        log.info { "threadLocal: ${threadLocal.get()}" }
        log.info { "coroutine name: ${coroutineContext[CoroutineName]}" }
    }.join()  // main이 launch 완료까지 대기하도록
}