package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.coroutine_scope_function.sub6_async_problem_solve

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

/**
 * async 의 문제 해결 — coroutineScope + async
 *
 *
 * 아이디어
 *      - coroutineScope { ... } 로 여러 async 를 감싸고, 그 안에서 await 합산까지 처리한다.
 *      - block 의 마지막 표현 값이 coroutineScope 의 반환값이 되어 result 로 그대로 받을 수 있다.
 *      - async 들이 모두 끝나야 coroutineScope 가 끝나도록 structured concurrency 가 자동 보장된다.
 *
 *
 * 핵심: 왜 300ms 가 아니라 약 100ms 만 걸리는가
 *      - async 는 비동기 builder. 호출되는 그 순간부터 각자 coroutine 이 시작된다.
 *      - 세 async 가 거의 같은 시각에 시작 → 각자 delay(100) 을 동시에 흘려보낸다.
 *      - 그 다음 줄의 .await() 들은 이미 끝난 (혹은 곧 끝날) 결과를 모으는 동기화 포인트 역할만 한다.
 *      - 그래서 전체 경과 시간은 sequential 300ms 가 아니라 parallel 약 100ms.
 *      - 즉 coroutineScope 가 "여러 async 가 병렬로 돌고, 모두 합류할 때까지 기다리는" 동기화 포인트 역할을 한다.
 *
 *
 * 효과
 *      - 묶어주는 outer launch / 명시적 join 패턴이 사라짐.
 *      - "여러 async 의 결과를 모아 그 합을 하나의 값으로 받는다" 는 의도가 코드 모양 그대로 드러남.
 *      - coroutineScope 가 자식 async 들의 lifecycle 을 책임지므로 await 누락 같은 lifecycle leak 위험이 줄어든다.
 *
 *
 * 출력
 *      [DefaultDispatcher-worker-2 @coroutine#2] - step1
 *      [DefaultDispatcher-worker-2 @coroutine#2] - result: 600
 *      [DefaultDispatcher-worker-2 @coroutine#2] - step2
 *
 *      → step1 과 result 사이의 실제 경과 시간은 약 100ms 수준 (300ms 가 아님).
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = EmptyCoroutineContext).launch {

            log.info { "step1" }

            val result = coroutineScope {
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

                deferred1.await() +
                        deferred2.await() +
                        deferred3.await()
            }
            log.info { "result: $result" }

            log.info { "step2" }
        }
        job.join()
    }
}
