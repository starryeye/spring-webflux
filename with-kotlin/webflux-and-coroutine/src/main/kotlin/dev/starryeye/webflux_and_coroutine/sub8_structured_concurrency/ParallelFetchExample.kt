package dev.starryeye.webflux_and_coroutine.sub8_structured_concurrency

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * 두 외부 API 를 동시에 호출하고 결과를 합치는 가장 흔한 패턴.
 *
 * 순차 (총 ~200ms)
 *      val a = fetchA()
 *      val b = fetchB()
 *
 * 병렬 (총 ~100ms) - async + await
 *      coroutineScope {
 *          val a = async { fetchA() }
 *          val b = async { fetchB() }
 *          a.await() to b.await()
 *      }
 */
private val log = LoggerFactory.getLogger("ParallelFetchExample")

private suspend fun fetchA(): String { delay(100); return "A" }
private suspend fun fetchB(): String { delay(100); return "B" }

suspend fun fetchSequential(): Pair<String, String> {
    val a = fetchA()
    val b = fetchB()
    return a to b
}

suspend fun fetchParallel(): Pair<String, String> = coroutineScope {
    val a = async { fetchA() }
    val b = async { fetchB() }
    a.await() to b.await()
}

fun main() = runBlocking {
    val seqElapsed = measure { fetchSequential() }
    log.info("sequential: {} ms", seqElapsed)

    val parElapsed = measure { fetchParallel() }
    log.info("parallel:   {} ms", parElapsed)
}

private suspend inline fun measure(block: () -> Unit): Long {
    val start = System.currentTimeMillis()
    block()
    return System.currentTimeMillis() - start
}
