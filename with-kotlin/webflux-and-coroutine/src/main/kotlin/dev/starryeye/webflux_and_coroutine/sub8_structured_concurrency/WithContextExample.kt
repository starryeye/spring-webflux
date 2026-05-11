package dev.starryeye.webflux_and_coroutine.sub8_structured_concurrency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

/**
 * withContext - dispatcher 잠깐 전환.
 *
 * 가장 흔한 사용처
 *      WebFlux 컨트롤러 / mono { } 빌더 안에서 블로킹 호출을 어쩔 수 없이 해야 할 때
 *      withContext(Dispatchers.IO) { ... } 로 감싸 이벤트 루프 스레드를 보호한다.
 *
 *          @GetMapping
 *          suspend fun fetch(): String = withContext(Dispatchers.IO) {
 *              jdbcTemplate.queryForObject(...)   // blocking JDBC 를 어쩔 수 없이 호출
 *          }
 *
 *      여기서 핵심은 "withContext 안의 블록이 끝나면 원래 dispatcher 로 자동 복귀" 한다는 것.
 */
private val log = LoggerFactory.getLogger("WithContextExample")

private fun blockingWork(): String {
    Thread.sleep(50) // 일부러 블로킹
    return "result"
}

suspend fun runWithContext(): String {
    log.info("before withContext - thread: {}", Thread.currentThread().name)
    val result = withContext(Dispatchers.IO) {
        log.info("inside withContext - thread: {}", Thread.currentThread().name)
        blockingWork()
    }
    log.info("after  withContext - thread: {}", Thread.currentThread().name)
    return result
}

fun main() = runBlocking {
    val r = runWithContext()
    log.info("result = {}", r)
}
