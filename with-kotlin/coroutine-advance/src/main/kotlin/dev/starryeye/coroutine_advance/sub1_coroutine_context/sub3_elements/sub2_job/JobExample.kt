package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub2_job

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * CoroutineContext 의 종류 (2) - Job
 *
 * 주의 - 이 페이지는 "CoroutineContext 의 한 Element 로서의 Job" 만 짧게 다룬다.
 *      Job 의 세부 동작 (상태 전이, parent-children 트리, 취소 전파, 예외 전파, SupervisorJob,
 *      Deferred 와의 관계 등) 은 뒤의 "CoroutineScope" 세션에서 더 자세히 다룬다.
 *      여기서는 "Job 인터페이스가 어떤 모양이고, context 안에서 어떻게 꺼내 쓰는지" 정도만 본다.
 *
 *
 * Job 이란
 *      - "Coroutine 의 생명주기를 표현/관리" 하는 CoroutineContext.Element 이다.
 *      - 모든 coroutine 은 자기 context 안에 정확히 하나의 Job 을 들고 있고,
 *          이 Job 이 곧 그 coroutine 의 "상태 / 시작 / 취소 / 부모-자식 관계" 의 핸들이 된다.
 *      - launch / async 같은 coroutine builder 는 "새 Job 을 만들어서 부모 Job 의 자식으로 매다는" 동작을 한다.
 *          -> structured concurrency 의 뼈대가 결국 이 Job 트리.
 *
 * 인터페이스 시그니처
 *      public interface Job : CoroutineContext.Element {
 *          public companion object Key : CoroutineContext.Key<Job>
 *
 *          // ---- state query ----
 *          @ExperimentalCoroutinesApi
 *          public val parent: Job?
 *          public val isActive: Boolean
 *          public val isCompleted: Boolean
 *          public val isCancelled: Boolean
 *
 *          // ---- state update ----
 *          public fun start(): Boolean
 *          public fun cancel(cause: CancellationException? = null)
 *
 *          public val children: Sequence<Job>
 *          // ... (join, invokeOnCompletion 등도 있지만 이 페이지에선 생략)
 *      }
 *
 * 특징
 *      - state query
 *          - isActive    : "지금 실행 중인가" (start 후 ~ 끝나기 전)
 *          - isCompleted : "끝났는가" (정상 종료, 예외 종료, 취소 모두 포함해서 "더 이상 실행되지 않음")
 *          - isCancelled : "취소되었는가"
 *          - parent      : 부모 Job (없으면 null. @ExperimentalCoroutinesApi)
 *      - state update
 *          - start()     : Lazy 로 만들어 둔 Job 을 명시적으로 시작 (이미 시작됐으면 false)
 *          - cancel()    : Job 과 그 자식들을 취소 (CancellationException 으로 협조적 취소)
 *      - 트리 탐색
 *          - children    : 자식 Job 들을 Sequence 로 노출 -> 부모에서 자식 트리를 훑을 수 있음
 *      - companion object Key
 *          - coroutineContext[Job] 로 꺼낼 때 쓰이는 Key. CoroutineName 과 동일한 패턴.
 *
 *
 * 예제에서 보여주는 것
 *      - runBlocking 의 receiver 에서 coroutineContext[Job] 으로 부모 Job 을 꺼내 본다.
 *      - launch 가 만들어 준 자식 Job 의 시작/실행/완료 시점에 isActive / isCompleted 가 어떻게 변하는지 본다.
 *      - 부모 Job 의 children 안에 자식 Job 이 들어가 있는 것을 확인한다.
 *
 *      더 깊은 동작 (취소 전파, SupervisorJob, Deferred, join 등) 은 뒤의 CoroutineScope 세션에서 다룬다.
 */
private val log = KotlinLogging.logger {}

fun main() = runBlocking {
    // (1) 현재 coroutine 의 Job 꺼내기 - context[Job] 으로 접근
    val parentJob: Job = coroutineContext[Job]!!
    log.info { "parentJob class    = ${parentJob::class.simpleName}" }   // BlockingCoroutine
    log.info { "parentJob.isActive = ${parentJob.isActive}" }            // true (실행 중)

    // (2) launch 는 새 Job 을 만들어 parentJob 의 자식으로 매단다.
    val child: Job = launch {
        log.info { "  child started, isActive = ${coroutineContext[Job]!!.isActive}" } // true
        delay(50)
    }

    // launch 직후엔 자식이 막 매달린 상태 -> children 에 보인다.
    log.info { "parent.children    = ${parentJob.children.toList()}" }
    log.info { "child.isActive (직후) = ${child.isActive}" }

    // (3) 자식이 끝날 때까지 기다린 뒤 상태 변화를 본다.
    child.join()
    log.info { "child.isActive (완료 후)    = ${child.isActive}" }      // false
    log.info { "child.isCompleted (완료 후) = ${child.isCompleted}" }   // true
    log.info { "child.isCancelled (완료 후) = ${child.isCancelled}" }   // false (정상 종료)
}
