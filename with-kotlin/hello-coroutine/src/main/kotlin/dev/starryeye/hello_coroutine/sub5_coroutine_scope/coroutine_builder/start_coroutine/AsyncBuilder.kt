package dev.starryeye.hello_coroutine.sub5_coroutine_scope.coroutine_builder.start_coroutine

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * async
 *
 * - 그룹: 새 코루틴을 "시작"하는 빌더
 * - 호출 위치: CoroutineScope 안
 * - 반환: Deferred<T>
 * - 호출자 블로킹: 즉시 반환, 나중에 await 로 결과를 받음
 */
private val log = KotlinLogging.logger {}

fun main() = runBlocking {
    log.info { "runBlocking: async 호출 전" }

    val deferred = async {
        delay(100)
        log.info { "async: 새 코루틴에서 결과값 생성" }
        "async result"
    }

    log.info { "runBlocking: async 는 Deferred<T> 를 즉시 반환한다. deferred=$deferred" }

    val result = deferred.await()
    log.info { "runBlocking: await 결과 = $result" }
}
