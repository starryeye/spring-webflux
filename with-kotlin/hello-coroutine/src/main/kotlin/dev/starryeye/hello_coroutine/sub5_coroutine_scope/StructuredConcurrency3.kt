package dev.starryeye.hello_coroutine.sub5_coroutine_scope

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 아래는 structured concurrency 의 예제이다.
 *
 * 부모 coroutine 에서 cancel 되면 자식 coroutine 으로 cancel 전파된다.
 *      delay 함수는 cancel 이 전파되면 CancellationException 을 발생시키고 cancel 됨.
 *
 * 아래는 스레드 1개로 동작한다.
 */
private val log = KotlinLogging.logger {}

private suspend fun structured() = coroutineScope {
    log.info { "step 1" }

    launch {
        try {
            delay(1000)
            log.info { "Finish launch1" }
        } catch (e: CancellationException) {
            log.info { "job1 cancelled" }
        }

    }

    log.info { "step 2" }

    launch {
        try {
            delay(500)
            log.info { "Finish launch2" }
        } catch (e: CancellationException) {
            log.info { "job2 cancelled" }
        }
    }

    delay(100)
    log.info { "step 3" }
    this.cancel()
}

fun main() = runBlocking {
    log.info { "Start runBlocking" }

    try {
        structured()
    } catch (e: CancellationException) {
        log.info { "job cancelled" }
    }

    log.info { "Finish runBlocking" }
}
