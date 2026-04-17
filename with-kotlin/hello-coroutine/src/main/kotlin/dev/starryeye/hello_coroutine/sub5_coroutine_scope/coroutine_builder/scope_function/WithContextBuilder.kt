package dev.starryeye.hello_coroutine.sub5_coroutine_scope.coroutine_builder.scope_function

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * withContext
 *
 * - 그룹: 새 코루틴을 "시작하지 않는" 빌더(scope function)
 * - 호출 위치: suspend 함수 안
 * - 반환: T
 * - 특징: 새 코루틴을 시작하는 빌더가 아니라, 현재 suspend 흐름의 컨텍스트를 바꿔 실행
 */
private val log = KotlinLogging.logger {}

suspend fun withContextExample(): String {
    log.info { "withContext: 호출 전" }

    val result = withContext(Dispatchers.Default) {
        log.info { "withContext: Dispatchers.Default 컨텍스트에서 실행" }
        "withContext result"
    }

    log.info { "withContext: 호출 후" }
    return result
}

fun main() = runBlocking {
    val result = withContextExample()
    log.info { result }
}
