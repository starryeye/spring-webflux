package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.supervisor_scope

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.EmptyCoroutineContext

/**
 * supervisorScope 예제 (async)
 *
 *
 * 목표
 *      "여러 작업을 병렬로 돌리되, 일부가 실패해도 성공한 것만이라도 모아 쓰고 싶다" 는 패턴.
 *          - async 자식의 예외는 위로 전파되지 않고 Deferred 에 보관되었다가 await() 자리로 던져진다.
 *          - 따라서 await() 를 try-catch 로 감싸면, 실패한 작업만 골라서 처리하고 나머지는 정상 수집할 수 있다.
 *
 *
 * coroutineScope 와의 차이
 *      - coroutineScope 였다면 deferred2 가 실패하는 순간 그 실패가 위로 전파되어
 *        형제(deferred1, deferred3)와 scope 전체가 cancel 되어 버린다. (성공한 것도 못 건짐)
 *      - supervisorScope 라서 deferred2 의 실패가 형제를 건드리지 않고,
 *        deferred2.await() 에서만 예외로 드러난다. → 그 자리에서 try-catch 로 흡수.
 *
 *
 * 핵심
 *      - async 의 예외는 "던져지는 시점" 이 await() 다. (즉시 터지는 게 아님)
 *      - supervisorScope 가 그 예외의 "위로 전파" 만 막아주는 것이지, 예외 자체를 없애주진 않는다.
 *        → await() 하는 쪽에서 반드시 직접 처리해야 한다.
 *
 *
 * 출력
 *      [DefaultDispatcher-worker-1 @coroutine#2] - result1: 100
 *      [DefaultDispatcher-worker-1 @coroutine#2] - deferred2 failed: deferred2 failed → 기본값 0 으로 대체
 *      [DefaultDispatcher-worker-1 @coroutine#2] - result3: 300
 *      [DefaultDispatcher-worker-1 @coroutine#2] - sum: 400
 *
 *      → deferred2 만 실패해 0 으로 대체되고, 나머지 100 + 300 은 그대로 살아 합계 400.
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = EmptyCoroutineContext).launch {

            val sum = supervisorScope {
                val deferred1 = async {
                    delay(timeMillis = 100)
                    100
                }
                val deferred2 = async<Int> {
                    delay(timeMillis = 100)
                    throw IllegalStateException("deferred2 failed")
                }
                val deferred3 = async {
                    delay(timeMillis = 100)
                    300
                }

                val result1 = deferred1.await()
                log.info { "result1: $result1" }

                // 실패하는 자식만 await() 자리에서 try-catch 로 흡수 (형제는 영향 없음)
                val result2: Int = try {
                    deferred2.await()
                } catch (e: IllegalStateException) {
                    log.info { "deferred2 failed: ${e.message} → 기본값 0 으로 대체" }
                    0
                }

                val result3 = deferred3.await()
                log.info { "result3: $result3" }

                result1 + result2 + result3
            }

            log.info { "sum: $sum" }
        }

        job.join()
    }
}
