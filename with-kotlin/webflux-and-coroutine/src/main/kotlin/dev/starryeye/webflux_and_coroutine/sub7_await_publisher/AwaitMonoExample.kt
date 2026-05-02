package dev.starryeye.webflux_and_coroutine.sub7_await_publisher

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * Mono / Flux 응답 타입을 suspend 함수 안에서 받는 예제.
 *
 * 외부 API 호출 비용 없이 보여주기 위해 Mono / Flux 를 직접 만든다.
 * 실제로는 WebClient / R2DBC / Reactor Kafka 등이 반환하는 Mono / Flux 가 그 자리에 들어간다.
 */
private val log = LoggerFactory.getLogger("AwaitMonoExample")

// 외부에서 받았다고 치는 Mono / Flux 반환 함수들
private fun fetchUserAsync(id: Long): Mono<String> =
    Mono.just("user-$id").delayElement(Duration.ofMillis(50))

private fun fetchUserOrEmpty(id: Long): Mono<String> =
    if (id < 0) Mono.empty() else Mono.just("user-$id")

private fun streamNotifications(userId: Long): Flux<String> =
    Flux.range(0, 3).map { "notif-$userId-$it" }




// suspend 함수에서 위 reactive API 들을 호출
private suspend fun loadUser(id: Long): String =
    fetchUserAsync(id).awaitSingle()                  // Mono -> 값 1개

private suspend fun loadUserOrNull(id: Long): String? =
    fetchUserOrEmpty(id).awaitSingleOrNull()          // Mono -> 0..1개 (빈 Mono 면 null)

private suspend fun loadAllNotifications(userId: Long): List<String> =
    streamNotifications(userId).collectList().awaitSingle()   // Flux -> List 로 한 번에

private fun streamNotificationsAsFlow(userId: Long): Flow<String> =
    streamNotifications(userId).asFlow()              // Flux -> Flow 로 (스트리밍 유지)





fun main() = runBlocking {
    log.info("loadUser(42)        = {}", loadUser(42L))
    log.info("loadUserOrNull(-1)  = {}", loadUserOrNull(-1L))
    log.info("loadAllNotifs(7)    = {}", loadAllNotifications(7L))

    log.info("streamAsFlow(7) ->")
    streamNotificationsAsFlow(7L).toList().forEach { log.info("  {}", it) }
}
