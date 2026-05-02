package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub4_key_element_and_operators

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * CoroutineContext 의 연산자 - get, plus, minusKey, fold
 *
 * 같은 sub4 의 CoroutineContextKeyElementExample 에서 Key, Element 의 구조를 봤다.
 *      이번에는 그 위에서 동작하는 연산자들을 본다.
 *
 *      public interface CoroutineContext {
 *          public operator fun <E : Element> get(key: Key<E>): E?       // 특정 Key 의 Element 를 반환 (없으면 null)
 *          public fun <R> fold(initial: R, operation: (R, Element) -> R): R
 *          public operator fun plus(context: CoroutineContext): CoroutineContext
 *                                                                       // 두 context 를 병합. 같은 Key 가 있으면 오른쪽 (인자) 으로 override
 *          public fun minusKey(key: Key<*>): CoroutineContext           // 주어진 Key 의 Element 를 제외한 context 를 반환
 *      }
 *
 *      - get(key)  : 코드에서는 보통 context[Key] 로 사용. 인덱싱 문법이 operator get 으로 풀린다.
 *      - plus(+)   : 같은 Key 의 Element 가 양쪽에 있다면 "오른쪽이 이긴다" (override).
 *      - minusKey  : 특정 Key 만 빼고 싶을 때 사용. 결과 context 의 형태도 자동으로 바뀐다.
 *      - fold      : context 안의 Element 들을 처음부터 끝까지 훑으며 누적한다 (Iterable.fold 와 같은 모양).
 *
 * 예제 - 이 파일이 보여주는 것
 *      (a) get - context[Key] 로 특정 Element 를 꺼낸다. 없으면 null.
 *      (b) plus - 같은 Key 의 Element 가 양쪽에 있을 때 "오른쪽이 이긴다 (override)" 동작 확인.
 *      (c) minusKey - Key 1개씩 제거하면서 결과 context 의 모양 변화 (CombinedContext -> Element -> EmptyCoroutineContext) 를 본다.
 *      (d) fold - 실제 runBlocking 의 coroutineContext 안에 어떤 Element 들이 들어있는지 훑어본다.
 */
private val log = KotlinLogging.logger {}

fun main() {
    // (a) get - context[Key] 로 Element 조회
    //      CoroutineName, UserId 는 등록되어 있으므로 값이 나오고, Job 은 등록된 적이 없으니 null 이다.
    val combined: CoroutineContext = CoroutineName(name = "alpha") +
            UserId(value = 42L) +
            Dispatchers.IO
    log.info { "combined[CoroutineName] = ${combined[CoroutineName]}" }
    log.info { "combined[UserId]        = ${combined[UserId]}" }
    log.info { "combined[Job]           = ${combined[Job]}" } // 등록된 적이 없으니 null

    // (b) plus 의 override 동작 - 같은 Key 의 Element 가 양쪽에 있다면 "오른쪽 (인자) 이 이긴다"
    //      a + b 라면, a 의 Element 들 위에 b 의 Element 들이 덧씌워진다고 이해하면 된다.
    val overridden: CoroutineContext = CoroutineName(name = "alpha") + CoroutineName(name = "beta")
    log.info { "overridden              = $overridden" }                         // beta 만 남는다
    log.info { "overridden[CoroutineName] = ${overridden[CoroutineName]}" }      // CoroutineName(beta)

    // (c) minusKey - Key 1개씩 제거하면서 모양 변화 관찰
    //      CombinedContext (3개) -> CombinedContext (2개) -> Element (1개) -> EmptyCoroutineContext (0개)
    val step0: CoroutineContext = combined
    val step1: CoroutineContext = step0.minusKey(UserId)                   // Element 2개 -> 여전히 CombinedContext
    val step2: CoroutineContext = step1.minusKey(CoroutineName)            // Element 1개 -> Element 자체
    val step3: CoroutineContext = step2.minusKey(Dispatchers.IO.key)       // Element 0개 -> EmptyCoroutineContext
    log.info { "step0 (combined)        = $step0  (${step0::class.simpleName})" }
    log.info { "step1 (-UserId)         = $step1  (${step1::class.simpleName})" }
    log.info { "step2 (-Name)           = $step2  (${step2::class.simpleName})" }
    log.info { "step3 (-Dispatcher)     = $step3  (${step3::class.simpleName})" }
    log.info { "step3 is Empty? ${step3 === EmptyCoroutineContext}" }

    // (d) fold - 실제 runBlocking 이 만든 CoroutineContext 안에 어떤 Element 들이 들어있는지 훑어본다.
    runBlocking(context = CoroutineName(name = "demo")) {
        val ctx = coroutineContext
        log.info { "----- runBlocking coroutineContext 의 Element 목록 -----" }
        ctx.fold(initial = 0) { index, element ->
            log.info { "  [$index] key=${element.key} element=$element" }
            index + 1
        }
        // 자주 쓰는 항목은 직접 꺼내볼 수도 있다.
        log.info { "Job          = ${ctx[Job]}" }
        log.info { "CoroutineName= ${ctx[CoroutineName]}" }
    }
}
