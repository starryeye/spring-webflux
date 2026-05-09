package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub5_custom_coroutine_context

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * CoroutineContext 의 종류 (5) - 커스텀 CoroutineContext 만들기
 *
 * 만드는 방법
 *      1) AbstractCoroutineContextElement 를 상속하는 클래스를 만들고
 *      2) 생성자에 자기 자신의 Key (companion object) 를 넘겨준다
 *      3) companion object Key : CoroutineContext.Key<MyContext> 를 선언
 *      이렇게만 하면 그 클래스가 CoroutineContext.Element 가 되어
 *      coroutineContext[MyContext] 로 꺼내 쓸 수 있게 된다.
 *
 *      kotlinx 의 CoroutineName, ReactorContext 등이 모두 이 패턴이다.
 *
 *
 * 예제
 *      - greeting 문자열을 들고 다니는 GreetingContext 를 직접 정의
 *      - launch 의 인자로 넘겨 자식 coroutine 에 전파됨을 확인
 *      - 중첩 launch 안에서 같은 Key (GreetingContext) 로 새 element 를 주입하면
 *          plus(+) override 규칙대로 그 아래부터는 새 값으로 보인다
 *      - 별도의 CoroutineScope(Dispatchers.IO + context) 를 만들어 띄워도
 *          그 scope 의 자식들에게도 동일하게 전파된다
 *
 *
 * 출력 예시 (순서는 dispatcher 에 따라 달라질 수 있음)
 *      [DefaultDispatcher-worker-1 @coroutine#3] - Hello     <- CoroutineScope(IO + context) 의 launch
 *      [DefaultDispatcher-worker-3 @coroutine#4] - Hello     <- 그 안의 nested launch
 *      [DefaultDispatcher-worker-2 @coroutine#5] - Hello     <- 그 안의 nested launch
 *      [main @coroutine#2] - Hello                            <- runBlocking 안의 launch(context)
 *      [main @coroutine#6] - Hola                             <- 위 안에서 launch(newContext) 로 override
 *      [main @coroutine#7] - Hola                             <- 그 안의 nested launch (override 된 값 상속)
 */
private val log = KotlinLogging.logger {}

// (1) AbstractCoroutineContextElement 를 상속해 CoroutineContext.Element 를 직접 만든다.
//      생성자에 GreetingContext (companion object Key) 를 넘겨주는 패턴이 핵심.
private class GreetingContext(
    private val greeting: String,
) : AbstractCoroutineContextElement(GreetingContext) {

    // (2) coroutineContext[GreetingContext] 로 꺼낼 때 쓰일 Key.
    //      class 자신이 Key 의 companion object 가 되도록 이름을 동일하게 가져간다.
    companion object Key : CoroutineContext.Key<GreetingContext>

    // (3) 이 element 만의 동작 - 들고 있는 greeting 을 출력
    fun greet() {
        log.info { greeting }
    }
}

fun main() = runBlocking {

    // base context 생성 - "Hello" 를 들고 있는 GreetingContext element
    val context = GreetingContext(greeting = "Hello")

    // launch 의 인자로 GreetingContext 를 주입 -> 자식 coroutine 의 context 에 그대로 들어간다
    launch(context = context) {
        // 부모로부터 받은 GreetingContext 가 보임 -> "Hello"
        coroutineContext[GreetingContext]?.greet()

        // 같은 Key 로 새 element 를 주입 -> plus 규칙으로 override 됨
        val newContext = GreetingContext(greeting = "Hola")
        launch(context = newContext) {
            // 이 자리부터는 "Hola"
            coroutineContext[GreetingContext]?.greet()

            // 그 안의 nested launch -> 따로 인자를 안 주면 부모 context 그대로 상속 -> "Hola"
            launch {
                coroutineContext[GreetingContext]?.greet()
            }
        }
    }

    // 별도의 CoroutineScope 에도 똑같이 전파된다.
    //      Dispatchers.IO + context (= GreetingContext("Hello")) 로 만든 scope.
    //      이 scope 의 자식들은 모두 "Hello" 를 본다.
    val job = CoroutineScope(context = Dispatchers.IO + context).launch {
        coroutineContext[GreetingContext]?.greet()       // "Hello"

        launch {
            coroutineContext[GreetingContext]?.greet()   // "Hello" (부모 상속)
        }

        launch {
            coroutineContext[GreetingContext]?.greet()   // "Hello" (부모 상속)
        }
    }

    // 별도 scope 에서 띄운 자식이 끝날 때까지 대기
    //      runBlocking 의 자식이 아니므로 join() 으로 명시적으로 기다려줘야 한다.
    job.join()
}
