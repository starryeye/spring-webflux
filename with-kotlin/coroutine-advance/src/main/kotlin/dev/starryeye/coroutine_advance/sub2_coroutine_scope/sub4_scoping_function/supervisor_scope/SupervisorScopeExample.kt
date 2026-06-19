package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.supervisor_scope

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

/**
 * supervisorScope 예제 (launch)
 *
 *
 * 목표
 *      supervisorScope 로 두 자식을 묶고, child1 이 도중에 예외로 실패해도
 *          - 형제 child2 는 cancel 되지 않고 끝까지 동작하며,
 *          - supervisorScope 자신도 정상 완료되어 "after supervisorScope" 까지 도달함을 본다.
 *
 *
 * 만약 coroutineScope 였다면
 *      - child1 의 실패가 위로 전파되어 child2 가 cancel 되고,
 *      - coroutineScope 자체도 실패 → outer launch 까지 실패 → "after supervisorScope" 는 출력되지 않는다.
 *      → 직접 supervisorScope 를 coroutineScope 로 바꿔 돌려보면 차이가 확연하다.
 *
 *
 * 예외 처리
 *      - supervisorScope 의 직접 자식 launch 는 root coroutine 처럼 취급되므로,
 *        처리되지 않은 예외는 부모로 던져지지 않고 context 의 CoroutineExceptionHandler 로 간다.
 *      - 그래서 outer scope 에 handler 를 심어 두면 child1 의 예외를 거기서 받아볼 수 있다.
 *
 *
 * 출력
 *      [DefaultDispatcher-worker-1 @coroutine#2] - child1: start
 *      [DefaultDispatcher-worker-2 @coroutine#3] - child2: start
 *      [DefaultDispatcher-worker-1 @coroutine#2] - handler caught: child1 failed
 *      [DefaultDispatcher-worker-2 @coroutine#3] - child2: done (살아남음)
 *      [DefaultDispatcher-worker-2 @coroutine#2] - after supervisorScope (scope 정상 완료)
 *
 *      → child1 이 50ms 시점에 터지지만 child2 는 100ms 까지 살아서 "done" 을 찍는다.
 *        자식 실패가 위로 전파되지 않았기 때문에 supervisorScope 도 정상적으로 빠져나온다.
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {

        val handler = CoroutineExceptionHandler { _, throwable ->
            log.info { "handler caught: ${throwable.message}" }
        }

        val job = CoroutineScope(context = Dispatchers.Default + handler).launch {

            supervisorScope {
                // child1: 도중에 실패
                launch {
                    log.info { "child1: start" }
                    delay(timeMillis = 50)
                    throw IllegalStateException("child1 failed")
                }

                // child2: 형제가 실패해도 살아남아 끝까지 동작
                launch {
                    log.info { "child2: start" }
                    delay(timeMillis = 100)
                    log.info { "child2: done (살아남음)" }
                }
            }

            // supervisorScope 가 정상 완료되어야 이 줄에 도달한다.
            log.info { "after supervisorScope (scope 정상 완료)" }
        }

        job.join()
    }
}
