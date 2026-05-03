package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub6_operators

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.EmptyCoroutineContext

/**
 * CoroutineContext 의 연산자 (1) - plus (+)
 *
 * 시그니처
 *      public operator fun plus(context: CoroutineContext): CoroutineContext
 *
 * 결합 규칙 - 누가 누구를 만나면 어떤 구현체가 되는가
 *      EmptyCoroutineContext + Element     = Element                (Empty 는 0 처럼 동작 -> 상대 그대로)
 *      Element                + Element     = CombinedContext        (단, 같은 Key 라면 -> 오른쪽 Element 로 대체)
 *      CombinedContext        + Element     = CombinedContext        (마찬가지로 같은 Key 라면 그 자리만 교체)
 *
 * override 규칙 - 같은 Key 가 양쪽에 있으면 "오른쪽이 이긴다"
 *      a + b 라고 쓰면 a 의 element 들 위에 b 의 element 들이 덧씌워진다.
 *      이 규칙 덕분에 "기본 context 위에 부분만 바꿔서" 새 context 를 쉽게 만들 수 있다.
 *          예) val ctx = baseContext + CoroutineName("temp")
 *
 * 이 파일이 보여주는 것 (PDF 121p 의 ContextPlusExample.kt 와 같은 흐름)
 *      step1: EmptyCoroutineContext             -> EmptyCoroutineContext 그 자체
 *      step2: empty + CoroutineName("custom")    -> Element (CoroutineName)
 *      step3: + CoroutineName("custom2")         -> 같은 Key 라 override -> 여전히 Element (CoroutineName)
 *      step4: + Dispatchers.IO                   -> 다른 Key -> CombinedContext (2개)
 *      step5: + Job()                            -> 다른 Key -> CombinedContext (3개)
 */
private val log = KotlinLogging.logger {}

fun main() {
    // step1 - 출발은 비어있는 context
    val context1 = EmptyCoroutineContext
    log.info { "context1 = $context1" }

    // step2 - Empty + Element = Element (그 Element 자기 자신)
    val element1 = CoroutineName(name = "custom name")
    val context2 = context1 + element1
    log.info { "context2 = $context2, class = ${context2::class.simpleName}" }
    log.info { "context2 === element1 ? ${context2 === element1}" } // true (Empty 는 상대를 그대로 돌려준다)

    // step3 - 같은 Key (CoroutineName) 의 Element 를 또 plus -> 오른쪽으로 override
    val element2 = CoroutineName(name = "custom name2")
    val context3 = context2 + element2
    log.info { "context3 = $context3, class = ${context3::class.simpleName}" } // 여전히 Element (CoroutineName)
    log.info { "context3[CoroutineName] = ${context3[CoroutineName]}" }       // CoroutineName(custom name2)

    // step4 - 다른 Key (Dispatchers.IO) 추가 -> CombinedContext (2개)
    val element3 = Dispatchers.IO
    val context4 = context3 + element3
    log.info { "context4 = $context4, class = ${context4::class.simpleName}" } // CombinedContext

    // step5 - 또 다른 Key (Job) 추가 -> CombinedContext (3개)
    val element4 = Job()
    val context5 = context4 + element4
    log.info { "context5 = $context5, class = ${context5::class.simpleName}" } // CombinedContext

    // 정리 - 결과로 들어 있는 Element 들을 fold 로 한 번 훑어본다.
    log.info { "----- context5 안의 Element 목록 -----" }
    context5.fold(initial = 0) { i, e ->
        log.info { "  [$i] $e (key=${e.key})" }
        i + 1
    }
}
