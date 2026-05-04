package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub1_structure.sub6_operators

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * CoroutineContext 의 연산자 (3) - get
 *
 * 시그니처
 *      public operator fun <E : Element> get(key: Key<E>): E?
 *
 * 핵심
 *      - operator fun get 으로 정의되어 있어서 두 가지 표기로 호출할 수 있다.
 *          (1) context[Key]            - 인덱싱 문법 (operator)
 *          (2) context.get(Key)        - 일반 메소드 호출
 *          두 형태는 완전히 동일한 호출이다.
 *      - Key 에 해당하는 Element 가 없다면 null 을 반환한다. (그래서 반환 타입이 E?)
 *      - CombinedContext 라면 element 부터 보고 없으면 left 쪽으로 재귀적으로 내려가며 찾는다.
 *
 * 이 파일이 보여주는 것
 *      context = CoroutineName + Dispatchers.IO        (Job 은 일부러 넣지 않음)
 *      element1 = context[CoroutineName]                -> CoroutineName(custom name)
 *      element2 = context.get(CoroutineDispatcher)      -> Dispatchers.IO   (Dispatcher 의 Key 는 CoroutineDispatcher)
 *      element3 = context[Job]                          -> null  (등록된 적 없음)
 */
private val log = KotlinLogging.logger {}

fun main() {
    val context = CoroutineName(name = "custom name") + Dispatchers.IO

    // (1) 인덱싱 문법 - context[Key]
    val element1 = context[CoroutineName]
    log.info { "element1 = $element1" } // CoroutineName(custom name)

    // (2) 메소드 호출 - context.get(Key)
    //      Dispatchers.IO 의 key 는 CoroutineDispatcher 이다. (구체 타입의 key 가 아니라 부모 abstract Key)
    val element2 = context.get(CoroutineDispatcher)
    log.info { "element2 = $element2" } // Dispatchers.IO

    // (3) 등록된 적 없는 Key 는 null
    val element3 = context[Job]
    log.info { "element3 = $element3" } // null
}
