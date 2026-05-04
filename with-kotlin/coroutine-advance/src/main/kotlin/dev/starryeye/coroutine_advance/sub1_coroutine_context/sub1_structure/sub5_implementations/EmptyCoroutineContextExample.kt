package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub1_structure.sub5_implementations

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * CoroutineContext 의 구현체 (1) - EmptyCoroutineContext
 *
 * 정의 (kotlin.coroutines.EmptyCoroutineContext) - 표준 라이브러리 소스
 *
 *      public object EmptyCoroutineContext : CoroutineContext, Serializable {
 *          private const val serialVersionUID: Long = 0
 *          private fun readResolve(): Any = EmptyCoroutineContext
 *
 *          public override fun <E : Element> get(key: Key<E>): E? = null
 *          public override fun <R> fold(initial: R, operation: (R, Element) -> R): R = initial
 *          public override fun plus(context: CoroutineContext): CoroutineContext = context
 *          public override fun minusKey(key: Key<*>): CoroutineContext = this
 *          public override fun hashCode(): Int = 0
 *          public override fun toString(): String = "EmptyCoroutineContext"
 *      }
 *
 * 핵심
 *      - CoroutineContext 인터페이스를 구현한 object (singleton)
 *      - Element 를 하나도 갖지 않은 "텅 빈" CoroutineContext 를 가리킨다.
 *      - 산술의 0 처럼 작동한다.
 *          + 와 만나면 상대를 그대로 돌려주고 (empty + ctx == ctx)
 *          - 도 자기 자신을 돌려준다 (empty.minusKey(any) == empty)
 *          fold 의 초기값을 그대로 돌려준다 (누적할 element 가 없음)
 *
 * 이 파일이 보여주는 것
 *      (a) get(Key) -> 항상 null
 *      (b) fold     -> initial 그대로 반환
 *      (c) plus     -> 상대 context 를 그대로 반환 (empty + X == X, X + empty == X)
 *      (d) minusKey -> 자기 자신 (= EmptyCoroutineContext) 그대로
 *      (e) singleton 확인 - kotlin.coroutines.EmptyCoroutineContext 는 object 이므로 == 가 아닌 === 로 비교해도 동일
 */
private val log = KotlinLogging.logger {}

fun main() {
    val empty: CoroutineContext = EmptyCoroutineContext
    log.info { "empty                 = $empty" }

    // (a) get -> null
    log.info { "empty[CoroutineName]  = ${empty[CoroutineName]}" } // null

    // (b) fold -> initial 그대로
    val foldResult = empty.fold(initial = "INIT") { acc, element -> "$acc/$element" }
    log.info { "empty.fold(\"INIT\")    = $foldResult" } // "INIT" (element 없음)

    // (c) plus -> 상대 그대로
    val name = CoroutineName(name = "alpha")
    val emptyPlusName = empty + name
    val namePlusEmpty = name + empty
    log.info { "empty + name          = $emptyPlusName  (===name? ${emptyPlusName === name})" }
    log.info { "name + empty          = $namePlusEmpty  (===name? ${namePlusEmpty === name})" }

    // (d) minusKey -> 자기 자신 (singleton)
    val minused = empty.minusKey(CoroutineName)
    log.info { "empty.minusKey(...)   = $minused (===empty? ${minused === empty})" }

    // (e) object singleton
    val anotherEmptyRef: CoroutineContext = EmptyCoroutineContext
    log.info { "two refs same?        ${empty === anotherEmptyRef}" } // true
}
