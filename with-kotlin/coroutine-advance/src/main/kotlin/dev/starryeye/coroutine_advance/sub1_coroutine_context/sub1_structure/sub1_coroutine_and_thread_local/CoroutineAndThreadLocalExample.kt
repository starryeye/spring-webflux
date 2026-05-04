package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub1_structure.sub1_coroutine_and_thread_local

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Coroutine 과 ThreadLocal
 *
 *      coroutine 안에서 "정보를 공유" 해야 한다면 어떻게 해야 할까?
 *          Java + Spring servlet stack 에서는 ThreadLocal 을 사용한다.
 *              하나의 스레드가 처음부터 끝까지 요청을 처리하기 때문에, 스레드에 정보를 묶어두면 어디서든 꺼내 쓸 수 있다.
 *          그런데 coroutine 은 "다른 스레드풀로 옮겨가며" 동작할 수 있다.
 *              즉, coroutine 의 실행 도중에 스레드가 바뀐다.
 *              이렇게 되면 ThreadLocal 은 사용하지 못함
 *
 * 동작 흐름
 *      1. main 스레드에서 ThreadLocal<String> 에 "hello" 를 set 한다.
 *      2. runBlocking { ... } 으로 코루틴을 시작한다.
 *          runBlocking 은 자기 자신을 호출한 스레드 (= main) 위에서 동작한다.
 *              따라서 runBlocking 안에서 Thread.currentThread().name 은 "main", greetingThreadLocal.get() 은 "hello" 가 나온다.
 *      3. runBlocking 안에서 launch(Dispatchers.IO) { ... } 를 띄운다.
 *          Dispatchers.IO 는 별도의 스레드풀 (DefaultDispatcher-worker-N) 에서 코루틴을 실행한다.
 *              즉, 여기 안의 코드는 main 이 아닌 worker 스레드에서 돈다.
 *          그 worker 스레드의 ThreadLocal 에는 아무 값도 set 된 적이 없다.
 *              따라서 greetingThreadLocal.get() 은 null 이 나온다.
 * 정리
 *      ThreadLocal 은 "스레드" 에 묶이고, coroutine 은 "스레드를 옮겨다닐 수 있다".
 *          따라서 ThreadLocal 은 coroutine 안에서 신뢰할 수 없는 정보 공유 수단이다.
 *      coroutine 에서는 그 대안으로 CoroutineContext 를 사용한다. (sub2 부터)
 */
private val log = KotlinLogging.logger {}

fun main() {
    val greetingThreadLocal = ThreadLocal<String>()
    greetingThreadLocal.set("hello") // main 스레드의 ThreadLocal 에만 "hello" 가 들어간다.

    runBlocking {
        // runBlocking 은 호출한 스레드 (main) 위에서 코루틴을 실행한다 -> ThreadLocal 접근 가능
        log.info { "thread: ${Thread.currentThread().name}" }
        log.info { "greeting: ${greetingThreadLocal.get()}" } // "hello"

        launch(Dispatchers.IO) {
            // Dispatchers.IO 의 worker 스레드로 옮겨와서 동작한다 -> ThreadLocal 에 값 없음
            log.info { "thread: ${Thread.currentThread().name}" }
            log.info { "greeting: ${greetingThreadLocal.get()}" } // null
        }
    }
}
