package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub3_io_and_default.dispatcher_inheritance_cases

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.ContinuationInterceptor

/**
 * Case 4
 *      부모 scope: dispatcher 없음 (CoroutineName 만 가진 scope)
 *      launch 인자: dispatcher 있음 (Dispatchers.IO)
 *      결과: launch 인자 dispatcher (IO).
 *
 * 구조 메모
 *      - Case 3 와 마찬가지 이유로 runBlocking 패턴을 쓸 수 없다 (runBlocking 은 항상 dispatcher 를 갖는다).
 *      - 별도 dispatcher 없는 CoroutineScope 를 만들고 그 위에서 launch.
 *      - 실제 부모는 parentScope. 바깥 runBlocking 은 .join() 호출용일 뿐.
 *
 * 관찰 포인트
 *      - 합친 context 에 dispatcher 가 있으므로 Default fallback 분기는 타지 않는다.
 *      - "부모에 dispatcher 가 없어도 launch 인자만으로 dispatcher 를 지정할 수 있다" 는 점 확인.
 *
 * 출력 예시
 *      [parent] dispatcher: null                                                    (parentScope 는 실행 중인 coroutine 이 아니라 thread 출력 없음)
 *      [child]  thread: DefaultDispatcher-worker-1 @parent#2, dispatcher: Dispatchers.IO
 */
private val log = KotlinLogging.logger {}

private fun CoroutineScope.dispatcher(): CoroutineDispatcher? =
    this.coroutineContext[ContinuationInterceptor] as? CoroutineDispatcher

fun main() {
    val parentScope = CoroutineScope(context = CoroutineName(name = "parent"))
    // 부모 scope: dispatcher 없음. launch 인자 dispatcher 만으로 자식 dispatcher 가 정해진다.
    log.info { "[parent] dispatcher: ${parentScope.dispatcher()}" }

    runBlocking {
        parentScope.launch(context = Dispatchers.IO) {
            // 자식 coroutine: 인자 IO → 인자 dispatcher 사용.
            log.info { "[child]  thread: ${Thread.currentThread().name}, dispatcher: ${this.dispatcher()}" }
        }.join()
    }
}
