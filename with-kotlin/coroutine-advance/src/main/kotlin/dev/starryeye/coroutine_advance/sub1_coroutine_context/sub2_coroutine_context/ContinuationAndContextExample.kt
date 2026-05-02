package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub2_coroutine_context

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

/**
 * Continuation 과 CoroutineContext - "왜 CoroutineContext 가 필요한가" 의 출발점
 *
 * sub1 에서 본 문제
 *      coroutine 은 다른 스레드풀로 옮겨다닐 수 있기 때문에 ThreadLocal 로 정보를 공유할 수 없다.
 *      그러면 suspend 함수들 사이에서 정보를 공유하려면 어떻게 해야 할까?
 *
 * 답 - Continuation 이 들고 다닌다.
 *      kotlin.coroutines 패키지의 Continuation interface 는 다음과 같이 생겼다.
 *
 *          public interface Continuation<in T> {
 *              public val context: CoroutineContext             // <- 핵심, Continuation 내부에는 CoroutineContext 를 포함한다.
 *              public fun resumeWith(result: Result<T>)
 *          }
 *
 *      Continuation 은 coroutine 안의 모든 suspend 함수에 전달된다. (CPS 변환의 결과로)
 *          그리고 Continuation 은 CoroutineContext 를 함께 들고 있다.
 *      따라서 suspend 함수는 어떤 스레드에서 실행되든 자기 인자로 받은 Continuation 을 통해
 *          항상 같은 CoroutineContext 에 접근할 수 있다.
 *
 * Continuation context 전파
 *      직접 CustomContinuation 을 만들어 본다.
 *          main 으로부터 전달받은 outer Continuation 의 context 를 그대로 자기 context 로 노출한다.
 *              -> override val context: CoroutineContext get() = completion.context
 *          이렇게 만들어 두면 CustomContinuation 을 인자로 받는 callee suspend 함수도
 *              outer 의 CoroutineContext 를 그대로 이어받게 된다.
 *      즉, "context 는 새로 만드는 게 아니라, 바깥에서 받은 것을 위임 (delegate) 해서 흘려보낸다" 가 핵심이다.
 *
 * 실행 흐름
 *      1. main 에서 outer Continuation 을 만든다. context 는 데모를 위해 단순히 EmptyCoroutineContext 를 사용한다.
 *      2. CustomContinuation 으로 감싸 inner 를 만든다. inner.context 는 outer.context 를 그대로 가리킨다.
 *      3. inner.resumeWith(...) 를 호출하면 결과를 보관하고 outer 에게 결과를 흘려보낸다 (= 위임).
 *      4. 두 Continuation 의 context 가 동일한 객체임을 로그로 확인한다.
 *
 * 정리
 *      - Continuation 은 단순히 "이어서 실행할 코드" 만 들고 있는 게 아니라 CoroutineContext 도 함께 들고 다닌다.
 *      - suspend 함수가 다른 스레드에서 재개되어도 CoroutineContext 는 Continuation 을 통해 그대로 이어진다.
 *      - sub3 에서는 이 context 에 "어떻게 접근하는가" 를 본다. (CoroutineScope.coroutineContext, Continuation.context, coroutineContext)
 */
private val log = KotlinLogging.logger {}

/**
 * 사용자 정의 Continuation
 *      바깥(=main)에서 받은 completion 의 context 를 그대로 자기 context 로 위임한다.
 *      resumeWith 도 결과만 가로채고 그대로 completion 에게 위임한다.
 *
 * 이 구조 덕분에 CustomContinuation 을 인자로 받는 callee suspend 함수는
 *      "한 번도 본 적 없는 outer 의 CoroutineContext" 를 자연스럽게 이어받는다.
 */
private class CustomContinuation(
    private val completion: Continuation<Any?>,
) : Continuation<Any?> {

    // 외부에서 받은 context 를 그대로 노출 -> "전파"
    override val context: CoroutineContext
        get() = completion.context

    override fun resumeWith(result: Result<Any?>) {
        log.info { "[CustomContinuation] resumeWith result=${result.getOrNull()}" }
        // 내부에서 추가 작업이 가능하지만, 결국에는 outer 에게 결과를 흘려보낸다.
        completion.resumeWith(result)
    }
}

fun main() {
    // 1. 바깥(main)에서 사용할 outer Continuation
    //      여기서는 데모를 위해 EmptyCoroutineContext 만 갖는다.
    //      실전에서는 runBlocking / launch 등이 여기에 Job, Dispatcher, CoroutineName 등을 채워둔다.
    val outer = object : Continuation<Any?> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<Any?>) {
            log.info { "[outer] resumeWith result=${result.getOrNull()}" }
        }
    }

    // 2. CustomContinuation 으로 감싸서 inner 를 만든다.
    val inner = CustomContinuation(completion = outer)

    // 3. context 가 outer 와 동일하게 유지되는지 확인
    //      두 객체의 hashCode 가 같다 -> "context 를 새로 만들지 않고 그대로 흘려보냈다" 는 의미.
    log.info { "outer.context: ${outer.context} (id=${System.identityHashCode(outer.context)})" }
    log.info { "inner.context: ${inner.context} (id=${System.identityHashCode(inner.context)})" }
    log.info { "same? ${outer.context === inner.context}" }

    // 4. inner 를 통해 결과를 resume 시키면 outer 까지 그대로 흘러간다.
    inner.resume(value = 100)
}
