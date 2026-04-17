package dev.starryeye.hello_coroutine.sub5_coroutine_scope.coroutine_builder.scope_function

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * coroutineScope
 *
 * - 그룹: 새 코루틴을 "시작하지 않는" 빌더(scope function)
 * - 호출 위치: suspend 함수 안
 * - 반환: T
 * - 특징: 내부 자식 코루틴이 모두 끝날 때까지 현재 코루틴을 suspend
 */
private val log = KotlinLogging.logger {}

suspend fun coroutineScopeExample(): String = coroutineScope {
    launch {
        delay(100)
        log.info { "coroutineScope: child launch 완료" }
    }

    val deferred = async {
        delay(50)
        "child async result"
    }

    log.info { "coroutineScope: 자식들이 끝날 때까지 이 scope 는 끝나지 않는다" }
    "coroutineScope result + ${deferred.await()}"
}

fun main() = runBlocking {
    val result = coroutineScopeExample()
    log.info { result }
}
