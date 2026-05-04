package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub2_propagation.sub2_with_context

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 *
 * 현재 context 에 특정 element 만 추가해서 실행하고 싶다면...
 * withContext 를 사용해보자.
 *
 * 시그니처
 *      public suspend fun <T> withContext(
 *          context: CoroutineContext,
 *          block: suspend CoroutineScope.() -> T
 *      ): T
 *
 * 핵심 동작 2가지
 *      1) 현재 coroutine 의 coroutineContext 에, 인자로 전달된 context 를 plus(+) 로 merge 한다.
 *          -> sub6 의 plus 규칙대로 "같은 Key 는 인자가 이긴다" 가 적용된다.
 *          -> 그래서 호출 측 context 의 대부분(CoroutineId, Dispatcher 등)은 그대로 유지하면서,
 *             전달한 파라미터 element 만 덮어쓰는 식으로 자연스럽게 부분 변경이 된다.
 *      2) merge 된 context 에 "새로운 Job" 을 만들어 주입한다.
 *          -> withContext 의 block 은 이 새 Job 위에서 실행되는 별개의 coroutine 이다.
 *             (block 이 끝나면 그 새 Job 도 정상 종료된다)
 *          -> 새 Job 은 부모 Job 의 자식으로 붙기 때문에 cancel/예외 전파는 그대로 동작한다.
 *
 * 그래서 무엇을 얻나
 *      - "잠깐 다른 element (예: 다른 Dispatcher, 다른 CoroutineName) 로 갈아끼워 실행" 하는 표준 방법이된다.
 *      - 새 coroutine 을 띄우긴 하지만 launch/async 와 달리 호출자는 결과 T 를 그대로 돌려받는,
 *          "suspend 함수처럼 보이는" API 다. (호출자 입장에서는 동기적으로 결과를 받음)
 *
 * 이 예제가 보여주는 것
 *      runBlocking { ... withContext(CoroutineName("withContext")) { ... } ... }
 *
 *      - 바깥(runBlocking) 에서 찍은 context vs 안쪽(withContext) 에서 찍은 context 를 비교한다.
 *      - 안쪽은 바깥 context 위에 CoroutineName("withContext") 가 덧씌워지고,
 *          UndispatchedCoroutine 라는 새 Job 이 들어가 있는 것이 보인다.
 *      - withContext 가 끝나면 다시 원래 runBlocking 의 context 로 돌아온 것을 한 번 더 찍어 확인한다.
 *
 * 출력
 *      [main @coroutine#1]   - context in runBlocking:
 *          [CoroutineId(1), "coroutine#1":BlockingCoroutine{Active}@..., BlockingEventLoop@...]
 *      [main @withContext#1] - context in withContext:
 *          [CoroutineId(1), CoroutineName(withContext),
 *              UndispatchedMarker, "withContext#1":UndispatchedCoroutine{Active}@..., BlockingEventLoop@...]
 *      [main @coroutine#1]   - context in runBlocking:
 *          [CoroutineId(1), "coroutine#1":BlockingCoroutine{Active}@..., BlockingEventLoop@...]
 *
 * 출력에서 읽어내는 포인트
 *      - 바깥쪽 두 줄은 동일하다 -> withContext 진입/탈출 후에도 호출자의 context 는 그대로 유지된다.
 *      - 안쪽 줄에서는
 *          - CoroutineName 이 "coroutine#1" -> "withMyContext#1" 로 바뀌었다 -> 같은 Key override (plus 규칙)
 *          - CoroutineId(1) 은 동일 -> Coroutine ID 같은 다른 element 는 그대로 merge 됨
 *          - Job 자리에 UndispatchedCoroutine 이 새로 들어와 있다 -> "새 Job 을 생성해서 주입" 의 흔적
 *          - 스레드는 [main @withMyContext#1] -> Dispatcher 를 갈아끼우지 않았으므로 같은 main 에서 실행됨
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        // 1) 진입 시점의 context - runBlocking 이 만들어 준 BlockingCoroutine Job 이 들어 있다.
        log.info { "context in runBlocking: $coroutineContext" }

        // 2) CoroutineName 만 갈아끼워 withContext 진입
        //      - 인자 context (CoroutineName("withContext")) 가 바깥 context 위에 plus 로 merge 된다.
        //      - 그리고 새 Job (UndispatchedCoroutine) 이 만들어져 block 의 context 로 주입된다.
        withContext(CoroutineName(name = "withMyContext")) {
            val ctx = this.coroutineContext
            log.info { "context in withContext: $ctx" }
        }

        // 3) withContext 가 끝나면, 원래 runBlocking 의 context 로 돌아온다.
        //      - 1) 의 출력과 동일한 것을 보면서 "withContext 는 호출자 context 를 변경하지 않는다" 를 확인.
        log.info { "context in runBlocking: $coroutineContext" }
    }
}
