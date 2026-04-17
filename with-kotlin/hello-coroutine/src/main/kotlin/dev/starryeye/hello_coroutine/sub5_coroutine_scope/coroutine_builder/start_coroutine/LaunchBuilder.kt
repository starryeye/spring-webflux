package dev.starryeye.hello_coroutine.sub5_coroutine_scope.coroutine_builder.start_coroutine

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * launch
 *
 * - 그룹: 새 코루틴을 "시작"하는 빌더
 * - 호출 위치: CoroutineScope 안
 * - 반환: Job
 * - 호출자 블로킹: 즉시 반환, 결과값 없음(fire-and-forget)
 */
private val log = KotlinLogging.logger {}

fun main() = runBlocking {
    log.info { "runBlocking: launch 호출 전" }

    val job = launch {
        delay(100)
        log.info { "launch: 새 코루틴에서 실행, 결과값은 없다" }
    }

    log.info { "runBlocking: launch 는 Job 을 즉시 반환한다. job=$job" }

    job.join()
    log.info { "runBlocking: join 으로 launch 완료를 기다림" }
}
