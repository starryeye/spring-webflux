package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub4_key_element_and_operators

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * CoroutineContext 의 내부 구조 - Key, Element
 *
 * "CoroutineContext 는 Element 들의 모음이다. (key 로 검색되는 Map 같은 것)"
 *      CoroutineContext 는 여러 Element 를 담는다.
 *          하나의 코루틴은 보통 Job, CoroutineDispatcher, CoroutineName, CoroutineExceptionHandler 같은 여러 Element 를 함께 들고 다닌다.
 *      각 Element 는 자기 Key 를 가지고 있고, Key 로 Element 를 찾을 수 있다.
 *
 * 1. Key 와 Element
 *
 *      public interface Key<E : Element>
 *      public interface Element : CoroutineContext {
 *          public val key: Key<*>                                       // 자기 자신을 식별하는 Key
 *          public override operator fun <E : Element> get(key: Key<E>): E?
 *          public override fun <R> fold(initial: R, operation: (R, Element) -> R): R
 *          public override fun minusKey(key: Key<*>): CoroutineContext
 *      }
 *
 *      - Key<E>      : Element 를 구분하는 식별자. 보통 Element 의 companion object 로 정의한다.
 *                      예) CoroutineName.Key, Job.Key, CoroutineDispatcher.Key, ...
 *      - Element     : "그 자체로도 CoroutineContext 인" 단일 항목.
 *                      Element 는 자기 key 와 일치하면 자기 자신을 반환하는 get / fold / minusKey 의 default 구현을 갖는다.
 *
 * 2. CoroutineContext 의 구현체 분류 (Element 의 개수에 따라)
 *      CoroutineContext 는 CoroutineContext 의 상태에 따라서 서로 다른 객체(여러 구현체)로 존재할 수 있다.
 *
 *      EmptyCoroutineContext     : Element 가 0개. context 가 비어있는 상태 (singleton).
 *      Element                   : Element 가 1개. Element 자체가 곧 그 context.
 *      CombinedContext           : Element 가 2개 이상. 두 context 를 + 로 결합하면 내부적으로 만들어진다.
 *
 *      즉, 연산자로 합치면 합쳐진 결과가 EmptyCoroutineContext / Element / CombinedContext 중 하나로 자동 결정된다.
 *      구체적인 연산자 동작은 같은 sub4 의 CoroutineContextOperatorsExample 에서 본다.
 *
 * 예제 - 이 파일이 보여주는 것
 *      (a) Element 1개짜리 context 가 곧 Element 자기 자신 임을 확인한다.
 *      (b) 커스텀 Element 를 만들어 본다 (UserId).
 *          companion object 로 Key 를 정의하고, AbstractCoroutineContextElement(Key) 를 상속해 Element 를 구현한다.
 *      (c) plus 로 여러 Element 를 합치면 CombinedContext 가 된다는 것을 확인하고, context[Key] 로 Element 를 꺼내본다.
 *      (d) Element 가 0개인 EmptyCoroutineContext (singleton) 의 모양을 확인한다.
 */
private val log = KotlinLogging.logger {}

/**
 * 커스텀 Element
 *      AbstractCoroutineContextElement(key) 를 상속하면 Element 의 boilerplate (key 노출, get, fold, minusKey) 가 채워진다.
 *      Key 는 보통 companion object 로 두고 자기 Element 타입의 Key<UserId> 로 선언한다.
 */
class UserId(val value: Long) : AbstractCoroutineContextElement(UserId) {
    companion object Key : CoroutineContext.Key<UserId>

    override fun toString(): String = "UserId($value)"
}

fun main() {
    // (a) Element 1개짜리 context = Element 그 자체
    val onlyName: CoroutineContext = CoroutineName(name = "alpha")
    log.info { "onlyName            = $onlyName" }
    log.info { "onlyName[CoroutineName] = ${onlyName[CoroutineName]}" }
    log.info { "onlyName is Element? ${onlyName is CoroutineContext.Element}" }

    // (b) 사용자 정의 Element - UserId 도 그 자체로 CoroutineContext 다.
    val onlyUserId: CoroutineContext = UserId(value = 42L)
    log.info { "onlyUserId          = $onlyUserId" }
    log.info { "onlyUserId[UserId]  = ${onlyUserId[UserId]}" }
    log.info { "onlyUserId is Element? ${onlyUserId is CoroutineContext.Element}" }

    // (c) plus 로 합쳐서 Element 2개 이상 -> CombinedContext
    //      구체적인 plus / minusKey 동작은 CoroutineContextOperatorsExample 에서 다룬다.
    val combined: CoroutineContext = CoroutineName(name = "alpha") +
            UserId(value = 42L) +
            Dispatchers.IO
    log.info { "combined            = $combined" }
    log.info { "combined[CoroutineName] = ${combined[CoroutineName]}" }
    log.info { "combined[UserId]    = ${combined[UserId]}" }
    log.info { "combined is CombinedContext (class name) = ${combined::class.simpleName}" }

    // (d) Element 0개 - EmptyCoroutineContext (singleton)
    val empty: CoroutineContext = kotlin.coroutines.EmptyCoroutineContext
    log.info { "empty               = $empty" }
    log.info { "empty[CoroutineName] = ${empty[CoroutineName]}" } // 아무것도 없으니 null
}
