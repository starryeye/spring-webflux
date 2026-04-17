package dev.starryeye.hello_coroutine.sub5_coroutine_scope

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 아래는 structured concurrency 의 예제이다.
 *
 * CoroutineScope 는 코루틴이 실행되는 범위이다.
 *      해당 범위 안에서 실행된 launch, async 코루틴은 그 스코프의 자식 코루틴이 된다.
 *
 * CoroutineScope::coroutineScope
 * coroutineScope 함수는 현재 코루틴의 CoroutineContext 를 이어받아 새로운 CoroutineScope 를 만든다.
 *      이 스코프 안에서 실행한 자식 코루틴이 모두 완료될 때까지 coroutineScope 는 종료되지 않는다.
 *      자식 코루틴에서 예외가 발생하면 부모 스코프로 전파되고, 부모 스코프가 취소되면 자식 코루틴도 함께 취소된다.
 *
 * Structured Concurrency
 * 즉, 부모 작업과 자식 작업의 생명주기가 구조적으로 엮인다.
 *      그래서 structured 함수가 끝났다는 것은 launch1, launch2 작업까지 모두 끝났다는 의미이다.
 *
 * 아래는 스레드 1개로 동작한다.
 */
private val log = KotlinLogging.logger {}

private suspend fun structured() = coroutineScope {
    log.info { "step 1" }

    launch {
        delay(1000)
        log.info { "Finish launch1" }
    }

    log.info { "step 2" }

    launch {
        delay(100)
        log.info { "Finish launch2" }
    }

    log.info { "step 3" }
}

fun main() = runBlocking {
    log.info { "Start runBlocking" }

    structured()

    log.info { "Finish runBlocking" }
}
