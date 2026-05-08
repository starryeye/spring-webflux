package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub3_thread_local_element

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * CoroutineContext 의 종류 (3-2) - ThreadLocalElement / ThreadContextElement
 *
 *
 * CoroutineThreadLocalExample 문제 해결
 *
 *      ThreadLocal 자체를 CoroutineContext 의 element 로 만들어서 코루틴과 함께 전파시킨다.
 *      이걸 해주는 게 ThreadLocalElement (= ThreadContextElement 의 구현체) 와
 *      그것을 만드는 확장함수 ThreadLocal<T>.asContextElement(value).
 *
 *      동작 원리
 *          - 코루틴이 어떤 스레드에서 재개될 때마다
 *              -> 그 스레드의 ThreadLocal 에 element 가 가진 value 를 set 해준다 ("진입")
 *              -> 코루틴이 suspend / 종료되면 원래 값으로 복원한다 ("이탈")
 *          - 즉 "스레드는 갈아끼우더라도 ThreadLocal 값은 코루틴을 따라다니는" 효과.
 *
 * 관련 타입
 *      internal class ThreadLocalElement<T>(
 *          private val value: T,
 *          private val threadLocal: ThreadLocal<T>
 *      ) : ThreadContextElement<T> { ... } // ThreadLocalElement 는 ThreadContextElement 를 구현한다.
 *
 *      // ThreadLocal 의 확장함수 제공
 *      public fun <T> ThreadLocal<T>.asContextElement(
 *          value: T = get()
 *      ): ThreadContextElement<T> = ThreadLocalElement(value, this)
 *
 *      포인트
 *          - asContextElement() 의 기본값 = 현재 스레드 ThreadLocal.get() -> "지금 값 그대로 전파"
 *          - 인자로 다른 값을 넘기면 그 값으로 덮어쓸 수도 있다 (예: greeting.asContextElement("hoi")).
 *          - 자식 coroutine 에도 그대로 전파된다 (CoroutineContext 의 일부니까).
 *
 *
 * 이 예제가 보여주는 것
 *      runBlocking 안에서 ThreadLocal greeting 에 "hello" 를 set 한 뒤
 *          1) launch(Dispatchers.IO)                                      -> null   (전파 안 됨)
 *          2) Dispatchers.IO + greeting.asContextElement()                -> "hello" (현재 값 전파)
 *          3) Dispatchers.Default + greeting.asContextElement("hoi")      -> "hoi"   (다른 값으로 override)
 *          4) 그 안에서 또 launch { }                                       -> "hoi"   (자식 coroutine 에도 전파)
 *
 */
private val log = KotlinLogging.logger {}

fun main() = runBlocking {
    val greeting = ThreadLocal<String>()
    greeting.set("hello")

    // (1) 그냥 다른 dispatcher 로 보내면 ThreadLocal 은 새 스레드에 없다 -> null
    launch(context = Dispatchers.IO) {
        log.info { "greeting1: ${greeting.get()}" }    // null
    }.join()



    // (2) asContextElement() 의 기본값 = 현재 스레드 ThreadLocal.get() = "hello"
    //      coroutine 이 어디서 재개되든 그 스레드의 ThreadLocal 에 "hello" 가 set 된다.
    val aContext = Dispatchers.IO + greeting.asContextElement() // ThreadLocal 내부에서, ThreadLocalElement(value = "hello", threadLocal = this(greeting))
    launch(context = aContext) {
        log.info { "greeting2: ${greeting.get()}" }    // hello
    }.join()



    // (3) 인자로 다른 값을 넘기면 그 값을 ThreadLocal 에 set 해준다.
    val bContext = Dispatchers.Default + greeting.asContextElement(value = "hoi") // ThreadLocal 내부에서, ThreadLocalElement(value = "hoi", threadLocal = this(greeting))
    launch(context = bContext) {
        log.info { "greeting3: ${greeting.get()}" }    // hoi

        // (4) 자식 coroutine 에도 같은 ThreadContextElement 가 그대로 전파된다.
        launch {
            log.info { "greeting4: ${greeting.get()}" }    // hoi
        }
    }.join()
}
