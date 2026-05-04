package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub1_structure.sub6_operators

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * CoroutineContext 의 연산자 (2) - minusKey
 *
 * 시그니처
 *      public fun minusKey(key: Key<*>): CoroutineContext
 *
 * 결과의 형태 (제거 후 남은 Element 개수에 따라 구현체가 자동으로 바뀐다)
 *      CombinedContext - Element = CombinedContext        (남은 개수가 2개 이상이면)
 *      CombinedContext - Element = Element                (남은 개수가 1개이면 그 Element 자체)
 *      Element         - Element = EmptyCoroutineContext  (자기 자신을 제거하면 비어버림)
 *      minus 하려는 Key 가 없다면 -> 무시 (현재 context 그대로)
 *
 * 이 파일이 보여주는 것
 *      context1 = CoroutineName + Dispatchers.IO + Job   (CombinedContext, 3개)
 *      context2 = context1 - Job                          -> 2개 -> CombinedContext
 *      context3 = context2 - Job                          -> 없는 Key -> 그대로 (무시)
 *      context4 = context3 - CoroutineDispatcher          -> 1개 남음 -> Element (CoroutineName) 자체
 *      context5 = context4 - CoroutineName                -> 0개 남음 -> EmptyCoroutineContext
 */
private val log = KotlinLogging.logger {}

fun main() {
    // 출발 - 3개짜리 CombinedContext
    val context1 = CoroutineName(name = "custom name") + Dispatchers.IO + Job()
    log.info { "context1 = $context1, class = ${context1::class.simpleName}" }

    // (1) CombinedContext - Element  (남은 개수 2개) -> 여전히 CombinedContext
    val context2 = context1.minusKey(Job)
    log.info { "context2 = $context2, class = ${context2::class.simpleName}" }

    // (2) 같은 Key (Job) 를 또 빼본다 - 이미 없으니 무시 (context2 와 동일)
    val context3 = context2.minusKey(Job)
    log.info { "context3 = $context3, class = ${context3::class.simpleName}" }
    log.info { "context3 === context2 ? ${context3 === context2}" } // 보통 true (변경 없으니 그대로 반환)

    // (3) CombinedContext - Element  (남은 개수 1개) -> Element 자체로 축소
    //      CoroutineDispatcher 는 abstract 부모 Key 다. Dispatchers.IO 의 key 도 CoroutineDispatcher 이다.
    val context4 = context3.minusKey(CoroutineDispatcher)
    log.info { "context4 = $context4, class = ${context4::class.simpleName}" } // CoroutineName (Element)

    // (4) Element - Element  (남은 개수 0개) -> EmptyCoroutineContext
    val context5 = context4.minusKey(CoroutineName)
    log.info { "context5 = $context5, class = ${context5::class.simpleName}" } // EmptyCoroutineContext
}
