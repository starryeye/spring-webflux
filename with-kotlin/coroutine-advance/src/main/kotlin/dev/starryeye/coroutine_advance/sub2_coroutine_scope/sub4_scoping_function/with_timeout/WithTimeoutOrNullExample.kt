package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.with_timeout

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.EmptyCoroutineContext

/**
 * withTimeoutOrNull 예제
 *
 *
 * 목표
 *      withTimeout 과 동작은 같지만, 시간 초과 시 throw 대신 null 을 반환하는 점을 본다.
 *          - 초과 케이스 : null → "초과했음" 을 값으로 분기 처리 (try-catch 불필요).
 *          - 성공 케이스 : block 의 결과 값을 그대로 반환.
 *
 *
 * withTimeout 과의 차이
 *      - withTimeout(t)       : 초과 시 TimeoutCancellationException throw → try-catch 필요.
 *      - withTimeoutOrNull(t) : 초과 시 내부에서 그 예외를 잡아 null 로 바꿔준다 → if (result == null) 로 처리.
 *      - "초과를 예외로 다룰지 / null 값으로 다룰지" 의 차이일 뿐, 제한시간·협조적 취소 동작은 동일.
 *
 *
 * 출력
 *      [DefaultDispatcher-worker-1 @coroutine#2] - case1 start (1000ms 작업, 제한 500ms)
 *      [DefaultDispatcher-worker-1 @coroutine#2] - case1 result: null → 시간 초과로 처리
 *      [DefaultDispatcher-worker-1 @coroutine#2] - case2 start (100ms 작업, 제한 500ms)
 *      [DefaultDispatcher-worker-1 @coroutine#2] - case2 result: 100 → 정상 완료
 *
 *      → 초과한 case1 은 null, 시간 내 끝난 case2 는 결과 값(100).
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = EmptyCoroutineContext).launch {

            // case1) 제한시간 초과 → null
            log.info { "case1 start (1000ms 작업, 제한 500ms)" }
            val result1: Int? = withTimeoutOrNull(timeMillis = 500) {
                delay(timeMillis = 1000)
                100
            }
            log.info { "case1 result: ${result1 ?: "null → 시간 초과로 처리"}" }

            // case2) 시간 내 완료 → 결과 값
            log.info { "case2 start (100ms 작업, 제한 500ms)" }
            val result2: Int? = withTimeoutOrNull(timeMillis = 500) {
                delay(timeMillis = 100)
                100
            }
            log.info { "case2 result: $result2 → 정상 완료" }
        }

        job.join()
    }
}
