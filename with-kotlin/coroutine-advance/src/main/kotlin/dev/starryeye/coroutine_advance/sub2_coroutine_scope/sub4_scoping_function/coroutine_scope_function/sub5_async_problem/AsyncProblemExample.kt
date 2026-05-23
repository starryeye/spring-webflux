package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.coroutine_scope_function.sub5_async_problem

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

/**
 * async 의 문제
 *
 * 문제
 *      - 요구사항: "여러 deferred 가 정확히 step1 출력과 step2 출력 사이에 반환되어야 한다."
 *      - async 도 비동기 builder 라 호출 즉시 Deferred 만 반환한다.
 *      - 결과를 모으려면 deferred 변수를 일일이 받아두고, 마지막에 .await() 을 손으로 늘어놓아 합산해야 한다.
 *          - 자식이 많아질수록 .await() 을 N 번 쓰는 코드가 늘어나고, 누락 위험도 생긴다.
 *          - for loop 패턴에서 async 를 여러 번 띄우면 모든 deferred 를 list 로 모아 awaitAll() 같은 처리가 필요.
 *      - structured concurrency 가 코드 단계에서 자동으로 보장되지 않는 점은 launch 의 문제와 동일.
 *
 *
 * 출력 예시
 *      [DefaultDispatcher-worker-1 @coroutine#2] - step1
 *      [DefaultDispatcher-worker-1 @coroutine#2] - result: 600
 *      [DefaultDispatcher-worker-1 @coroutine#2] - step2
 *
 *      → 동작은 맞지만, .await() 3번을 명시적으로 호출했기 때문이다.
 *        다음 파일 (AsyncProblemExampleSolution1) 에서 coroutineScope 로 묶어 깔끔하게 해결하는 모습을 본다.
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = EmptyCoroutineContext).launch {
            val deferred1 = async {
                delay(timeMillis = 100)
                100
            }

            val deferred2 = async {
                delay(timeMillis = 100)
                200
            }

            val deferred3 = async {
                delay(timeMillis = 100)
                300
            }

            log.info { "step1" }
            val result = deferred1.await() +
                    deferred2.await() +
                    deferred3.await()
            log.info { "result: $result" }
            log.info { "step2" }
        }

        job.join()
    }
}
