package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub5_implementations

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * CoroutineContext 의 구현체 (3) - CombinedContext
 *
 * 정의 (kotlin.coroutines.CombinedContext) - 표준 라이브러리 소스 (요약)
 *
 *      internal class CombinedContext(
 *          private val left: CoroutineContext,
 *          private val element: Element
 *      ) : CoroutineContext, Serializable
 *
 *      - left      : 또 다른 CombinedContext 혹은 Element 를 가리킨다.
 *      - element   : "가장 최근에 추가된" Element 를 가리킨다.
 *      - internal class 라서 사용자가 직접 new 로 생성할 수는 없다.
 *          + 연산자가 합쳐주거나, 코루틴 빌더 (runBlocking / launch / async ...) 가 만들어 준다.
 *
 * 트리 구조 (PDF 120p)
 *      a + b + c + d + e 가 만들어 내는 모양
 *
 *           CombinedContext1 (left=CombinedContext2, element=Element1)
 *              /                              \
 *      CombinedContext2 (left=...)             Element1 (e)
 *           /                \
 *      CombinedContext3      Element2 (d)
 *         /          \
 *      Element4(b)   Element3(c)
 *
 *      - left 쪽으로 트리를 따라 내려가면서 누적되어 있고
 *      - 가장 최근 Element 가 항상 element 자리에 들어간다.
 *      - get(Key) 는 element 부터 보고 없으면 left 쪽으로 재귀적으로 내려가면서 찾는다.
 *
 * plus 동작 (CombinedContext 가 만들어지는 규칙)
 *      `current + newElement` 를 평가하면
 *      - current 안에 같은 Key 의 Element 가 이미 존재하면 -> 새 Element 로 override (그 자리만 교체)
 *      - 같은 Key 가 없으면 -> 현재 CombinedContext 를 left 로, 새 Element 를 element 로 갖는 새 CombinedContext 생성
 *
 *      구체적인 plus 의 패턴 별 결과 (Empty + X, Element + Element, ...) 는 sub6 의 ContextPlusExample 에서 본다.
 *
 * 이 파일이 보여주는 것
 *      (a) Element + Element = CombinedContext 인지 클래스 이름으로 확인.
 *      (b) 트리 모양 직접 흉내 - 3개를 합치면 어떻게 누적되는지 toString 으로 확인.
 *      (c) get(Key) 가 어떤 위치에 있는 Element 든 잘 찾아 내는지 확인.
 *      (d) plus 시 같은 Key override 동작 확인 (CoroutineName 두 번 -> 오른쪽이 이긴다).
 */
private val log = KotlinLogging.logger {}

fun main() {
    // (a) Element + Element = CombinedContext
    val combined2: CoroutineContext = CoroutineName(name = "alpha") + Dispatchers.IO
    log.info { "combined2 (2개)        = $combined2" }
    log.info { "combined2 클래스       = ${combined2::class.simpleName}" } // CombinedContext

    // (b) 3개 이상 합쳐도 CombinedContext (트리가 left 로 자라난다)
    val combined3: CoroutineContext = CoroutineName(name = "alpha") + Dispatchers.IO + Job()
    log.info { "combined3 (3개)        = $combined3" }
    log.info { "combined3 클래스       = ${combined3::class.simpleName}" } // CombinedContext

    // (c) get(Key) - 트리 어디에 있어도 찾아낸다.
    log.info { "combined3[CoroutineName] = ${combined3[CoroutineName]}" }
    log.info { "combined3[Job]           = ${combined3[Job]}" }
    log.info { "combined3[Dispatchers.IO.key] = ${combined3[Dispatchers.IO.key]}" }

    // (d) plus 시 같은 Key 가 있다면 override
    //      - 트리 구조 안에서 그 자리만 교체된다.
    val overridden: CoroutineContext = combined3 + CoroutineName(name = "beta") // CoroutineName 이 이미 존재
    log.info { "overridden            = $overridden" }
    log.info { "overridden[CoroutineName] = ${overridden[CoroutineName]}" } // CoroutineName(beta)
    log.info { "overridden 클래스      = ${overridden::class.simpleName}" }   // 여전히 CombinedContext
}
