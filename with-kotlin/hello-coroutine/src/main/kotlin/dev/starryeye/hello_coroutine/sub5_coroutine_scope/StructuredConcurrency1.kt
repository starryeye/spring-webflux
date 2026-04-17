package dev.starryeye.hello_coroutine.sub5_coroutine_scope

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CompletableFuture

/**
 * 기존의 Java 스타일 비동기 함수 동작
 *
 * main thread 1개, ForkJoinPool thread 2개로 동작한다.
 *
 * 아래는 non-structured concurrency 의 예제이다.
 *      자식 작업의 생명주기가 부모와 구조적으로 엮이지 않은" 상태, 즉 비구조적 동시성
 *          자식의 작업에서 예외가 발생해도 부모는 모른다.
 *          부모 작업이 취소되어도 자식으로까지 전파되지 않는다.
 */
private val log = KotlinLogging.logger {}

private fun nonStructured() {
    log.info { "step 1" }

    CompletableFuture.runAsync {
        Thread.sleep(1000)
        log.info { "Finish run1" }
    }

    log.info { "step 2" }

    CompletableFuture.runAsync {
        Thread.sleep(100)
        log.info { "Finish run2" }
    }

    log.info { "step 3" }
}

fun main() {
    log.info { "Start main" }

    nonStructured()

    log.info { "Finish main" }
    Thread.sleep(3000)   // 없으면 JVM이 먼저 종료되어 run1/run2를 못 봄
}