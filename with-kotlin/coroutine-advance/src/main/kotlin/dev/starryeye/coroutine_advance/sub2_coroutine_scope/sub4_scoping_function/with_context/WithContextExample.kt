package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.with_context

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * withContext 예제
 *
 * 목표
 *      withContext 를 활용하여, blocking 코드로 잠시 context 만 변경해서 실행시키고 싶을 때 사용.
 *          해당 예제에서는 잠시 다른 dispatcher 에서 실행시키기 위해 context 를 변경하는 withContext 사용함.
 *
 * thread 이름
 *      withContext 전 : worker-1
 *      withContext 안 : worker-1 (IO 와 Default 가 worker pool 을 공유하기 때문에 같은 worker 가 잡힐 수도 있음)
 *      withContext 후 : worker-3 (원래 dispatcher 로 다시 dispatch 되어 resume 되는데, 그때 어느 worker 가 잡힐지는 미정)
 *      그래서 withContext 전/후의 worker 번호가 다른 게 자연스럽다. dispatcher 는 같지만 같은 worker 가 보장되진 않는다.
 *
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = Dispatchers.Default).launch {
            log.info { "context in launch: $coroutineContext" }

            withContext(context = Dispatchers.IO) {
                log.info { "context in withContext: $coroutineContext" }
            }

            log.info { "context in launch again: $coroutineContext" }
        }

        job.join()
    }
}
