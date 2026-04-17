package dev.starryeye.hello_coroutine.sub5_coroutine_scope.coroutine_builder.start_coroutine

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * runBlocking
 *
 * - 그룹: 새 코루틴을 "시작"하는 빌더
 * - 호출 위치: 일반 함수(main 등)
 * - 반환: T
 * - 호출자 블로킹: 현재 스레드를 멈추고 내부 코루틴이 끝날 때까지 기다림
 */
private val log = KotlinLogging.logger {}

fun main() {
    log.info { "main: runBlocking 호출 전" }

    val result: String = runBlocking {
        log.info { "runBlocking: 새 코루틴 시작" }
        delay(100)
        "runBlocking result"
    }

    log.info { "main: result = $result" }
    log.info { "main: runBlocking 이 끝난 뒤에야 여기로 온다" }
}
