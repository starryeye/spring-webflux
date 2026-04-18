줘package dev.starryeye.hello_coroutine.sub5_coroutine_scope.coroutine_builder.scope_function

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

/**
 * supervisorScope
 *
 * - 그룹: 새 코루틴을 "시작하지 않는" 빌더(scope function)
 * - 호출 위치: suspend 함수 안
 * - 반환: T
 * - 특징: 내부 자식 코루틴이 모두 끝날 때까지 suspend,
 *        한 자식의 실패가 형제 자식에게 자동 전파되지 않음
 */
private val log = KotlinLogging.logger {}

suspend fun supervisorScopeExample(): String = supervisorScope {
    val failed = async {
        delay(50)
        log.info { "supervisorScope: child1 실패" }
        throw IllegalStateException("child1 failed")
    }

    val alive = async {
        delay(100)
        log.info { "supervisorScope: child2 는 계속 실행" }
        "child2 result"
    }

    val failedMessage = runCatching { failed.await() }
        .exceptionOrNull()
        ?.message
    val aliveResult = alive.await()

    "supervisorScope result, failed=$failedMessage, alive=$aliveResult"
}

fun main() = runBlocking {
    val result = supervisorScopeExample()
    log.info { result }
}
