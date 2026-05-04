package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub1_structure.sub4_key_and_element

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * CoroutineContext 의 내부 구조 - Key, Element (소개)
 *      여기서는 "Key 와 Element 가 무엇인지" 만 잡는다.
 *      구체적인 CoroutineContext 의 구현체 (EmptyCoroutineContext / Element / CombinedContext) 는 sub5 에서,
 *      연산자 (get / plus / minusKey) 는 sub6 에서 본다.
 *
 * "CoroutineContext 는 Element 들의 모음이다. (key 로 검색되는 Map 같은 것)"
 *      CoroutineContext 는 여러 Element 를 담는다.
 *          하나의 코루틴은 보통 Job, CoroutineDispatcher, CoroutineName, CoroutineExceptionHandler 같은 여러 Element 를 함께 들고 다닌다.
 *      각 Element 는 자기 Key 를 가지고 있고, Key 로 Element 를 찾을 수 있다.
 *
 *
 * Key 와 Element 인터페이스 정의
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
 * 예제 설명
 *      (a) 라이브러리가 제공하는 Element 1개짜리 context (CoroutineName) 가 곧 Element 자기 자신임을 확인.
 *      (b) 커스텀 Element 를 직접 만들어 본다 (UserId).
 *          companion object 로 Key 를 정의하고, AbstractCoroutineContextElement(Key) 를 상속해 Element 를 구현한다.
 */
private val log = KotlinLogging.logger {}

/**
 * 커스텀 Element - "이 코루틴이 어떤 사용자에 묶여 있는가" 를 의미하도록 의도함
 *
 *      AbstractCoroutineContextElement(key) 를 상속하면 Element 의 boilerplate (key 노출, get, fold, minusKey) 가 채워진다.
 *      Key 는 보통 companion object 로 두고 자기 Element 타입의 Key<UserId> 로 선언한다.
 */
class UserId(val value: Long) : AbstractCoroutineContextElement(UserId) {
    companion object Key : CoroutineContext.Key<UserId>

    override fun toString(): String = "UserId($value)"
}

fun main() {
    // (a) Element 1개짜리 context = Element 그 자체 (CoroutineName 도 Element 다)
    val onlyName: CoroutineContext = CoroutineName(name = "alpha")
    log.info { "onlyName              = $onlyName" }
    log.info { "onlyName[CoroutineName] = ${onlyName[CoroutineName]}" }
    log.info { "onlyName is Element?  ${onlyName is CoroutineContext.Element}" }

    // (b) 커스텀 Element - UserId 도 Element 이고, 그 자체로 CoroutineContext 다.
    val onlyUserId: CoroutineContext = UserId(value = 42L)
    log.info { "onlyUserId            = $onlyUserId" }
    log.info { "onlyUserId[UserId]    = ${onlyUserId[UserId]}" }
    log.info { "onlyUserId is Element? ${onlyUserId is CoroutineContext.Element}" }
}
