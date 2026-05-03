package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub5_implementations

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * CoroutineContext 의 구현체 (2) - Element 인터페이스를 구현한 구현체들
 *
 * Element 정의 한 번 더
 *      Element 는 "그 자체로도 CoroutineContext 인" 단일 항목이다.
 *      즉, Element 1개짜리 context 라고 봐도 된다.
 *      sub4 의 UserId 처럼 사용자가 직접 만들 수도 있고, 아래처럼 라이브러리가 제공하는 구현체도 있다.
 *
 * 라이브러리가 제공하는 대표적인 Element 구현체
 *      - CoroutineName             : 코루틴 이름 (디버깅 용도). companion object Key 를 가짐.
 *      - CoroutineDispatcher       : 어느 스레드 (풀) 에서 실행할지. Dispatchers.IO / Default / Unconfined / Main 등.
 *      - CoroutineExceptionHandler : 잡히지 않은 예외를 어떻게 처리할지.
 *      - Job                       : 코루틴의 lifecycle (Active / Cancelled / Completed) 을 들고 있는 핸들.
 *      - ThreadContextElement      : ThreadLocal 값을 코루틴 재개 시점마다 복원/저장하기 위한 훅.
 *
 * AbstractCoroutineContextElement
 *
 *      public abstract class AbstractCoroutineContextElement(
 *          public override val key: Key<*>
 *      ) : Element
 *
 *      Element 인터페이스의 boilerplate (key, get, fold, minusKey 의 default 동작) 를 미리 채워둔 추상 클래스.
 *      대부분의 라이브러리 구현체와 사용자 구현체는 이걸 상속해서 만든다.
 *
 *      예) CoroutineName 의 (요약된) 모양
 *
 *          public data class CoroutineName(
 *              val name: String
 *          ) : AbstractCoroutineContextElement(CoroutineName) {
 *              public companion object Key : CoroutineContext.Key<CoroutineName>
 *              override fun toString(): String = "CoroutineName($name)"
 *          }
 *
 *      참고
 *          AbstractCoroutineContextElement(CoroutineName) 와
 *          AbstractCoroutineContextElement(CoroutineName.Key) 는 사실 같다.
 *          companion object 의 이름이 Key 이기 때문에, "CoroutineName" 라고만 써도
 *              실제로는 그 안의 companion object (CoroutineName.Key) 객체를 가리킨다.
 *
 * 이 파일이 보여주는 것
 *      (a) CoroutineName, Job, Dispatchers.IO, CoroutineExceptionHandler 의 key 객체를 직접 꺼내 본다.
 *          companion object 이름만 써도 Key 인스턴스가 나온다는 것을 확인.
 *      (b) Element 1개 만으로도 CoroutineContext 로 동작 (context[Key] / minusKey).
 *      (c) runBlocking 의 coroutineContext 안에 어떤 Element 들이 들어 있는지 직접 꺼내 본다.
 */
private val log = KotlinLogging.logger {}

/**
 * 사용자 정의 Element 도 결국은 같은 패턴 - AbstractCoroutineContextElement(Key) 상속 + companion object Key.
 *      sub4 의 UserId 와 동일한 패턴이지만, "Element 구현체" 의 한 사례로 다시 한 번 보여준다.
 */
private class RequestId(val value: String) : AbstractCoroutineContextElement(RequestId) {
    companion object Key : CoroutineContext.Key<RequestId>

    override fun toString(): String = "RequestId($value)"
}

fun main() {
    // (a) 라이브러리가 정의해둔 Key 객체들을 꺼내본다.
    //      "CoroutineName" 이라고만 써도 companion object (= Key) 가 나온다.
    log.info { "CoroutineName.Key             = ${CoroutineName.Key}" }
    log.info { "CoroutineName 자체도 Key      = $CoroutineName" } // 같은 객체
    log.info { "Job(=Job.Key)                 = $Job" }
    log.info { "CoroutineExceptionHandler.Key = $CoroutineExceptionHandler" }
    log.info { "Dispatchers.IO.key            = ${Dispatchers.IO.key}" } // CoroutineDispatcher.Key

    // (b) Element 1개짜리 context 의 모양 확인
    val name: CoroutineContext = CoroutineName(name = "alpha")
    log.info { "name                  = $name" }
    log.info { "name[CoroutineName]   = ${name[CoroutineName]}" }
    log.info { "name.minusKey(CoroutineName) = ${name.minusKey(CoroutineName)}" } // EmptyCoroutineContext

    // 사용자 정의 Element 도 동일하게 동작
    val req: CoroutineContext = RequestId(value = "REQ-001")
    log.info { "req[RequestId]        = ${req[RequestId]}" }

    // (c) runBlocking 이 만드는 실제 context 안에 들어 있는 Element 들을 직접 꺼내 본다.
    runBlocking(context = CoroutineName(name = "demo")) {
        val ctx = coroutineContext
        log.info { "Job          = ${ctx[Job]}" }
        log.info { "CoroutineName= ${ctx[CoroutineName]}" }
        log.info { "Dispatcher   = ${ctx[kotlinx.coroutines.CoroutineDispatcher]}" }
        log.info { "ExceptionHdr = ${ctx[CoroutineExceptionHandler]}" } // 등록 안 했으니 null
    }
}
