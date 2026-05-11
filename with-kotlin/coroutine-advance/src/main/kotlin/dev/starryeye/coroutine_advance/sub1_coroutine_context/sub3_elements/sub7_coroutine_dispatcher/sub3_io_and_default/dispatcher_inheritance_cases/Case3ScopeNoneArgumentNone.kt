package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub3_io_and_default.dispatcher_inheritance_cases

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.ContinuationInterceptor

/**
 * Case 3
 *      부모 scope: dispatcher 없음 (CoroutineName 만 가진 scope)
 *      launch 인자: dispatcher 없음
 *      결과: Dispatchers.Default 가 fallback 으로 붙음.
 *
 * 구조 메모
 *      - 여기선 runBlocking 패턴을 쓸 수 없다. runBlocking 은 항상 자기 dispatcher (BlockingEventLoop 또는 인자로 받은 것) 를
 *        갖기 때문에 "부모에 dispatcher 가 없는 상태" 를 만들 수 없다.
 *      - 그래서 별도로 dispatcher 없는 CoroutineScope 를 만들어 그 위에서 launch.
 *      - 바깥 runBlocking { ... } 은 부모-자식 관계와 무관하다. parentScope.launch 가 던지는 Job 의 .join() 을
 *        부르려고 코루틴 컨텍스트가 필요해서 두른 것뿐. 실제 부모는 parentScope.
 *
 * 관찰 포인트
 *      - 이 케이스만 newCoroutineContext 의 마지막 분기를 탄다:
 *          `if (combined[ContinuationInterceptor] == null) debug + Dispatchers.Default`
 *      - 즉 "dispatcher 명시 안 함" 이라는 표현이 정확히 들어맞는 유일한 경우.
 *
 * 참고.
 *      sub5_root_coroutine 을 참고해보면 CoroutineScope.~ 로 실행하면 root coroutine 이다.
 *
 * 출력 예시
 *      [parent] dispatcher: null  (parentScope 는 실행 중인 coroutine 이 아니라 thread 출력 없음)
 *      [child]  thread: DefaultDispatcher-worker-1 @parent#2, dispatcher: Dispatchers.Default
 */
private val log = KotlinLogging.logger {}

private fun CoroutineScope.dispatcher(): CoroutineDispatcher? =
    this.coroutineContext[ContinuationInterceptor] as? CoroutineDispatcher

fun main() {
    val parentScope = CoroutineScope(context = CoroutineName(name = "parent"))
    // 부모 scope: dispatcher 없음. 자식이 상속할 dispatcher 가 없다는 점을 먼저 확인.
    log.info { "[parent] dispatcher: ${parentScope.dispatcher()}" }

    runBlocking {
        parentScope.launch {
            // 자식 coroutine: 인자 없음 + 부모 dispatcher 도 없음 → Default fallback.
            log.info { "[child]  thread: ${Thread.currentThread().name}, dispatcher: ${this.dispatcher()}" }
        }.join()
    }
}
