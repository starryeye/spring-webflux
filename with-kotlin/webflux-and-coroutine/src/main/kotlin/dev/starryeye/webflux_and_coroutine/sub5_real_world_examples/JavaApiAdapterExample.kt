package dev.starryeye.webflux_and_coroutine.sub5_real_world_examples

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/**
 * sub3 (CompletableFuture 반환) 이 자주 나오는 자리 - Java 모듈이 정의한 인터페이스.
 *
 * 멀티 모듈 프로젝트에서 인터페이스를 정의하는 모듈이 Java 인 경우가 많다.
 * Java 측이 다루기 좋은 시그니처는 CompletableFuture<T> 이고,
 * 우리는 그 시그니처를 만족하면서 본문은 코루틴으로 짜고 싶다.
 *
 * 같은 패턴이 AWS SDK v2 Async / Spring @Async / gRPC stub 에도 그대로 적용된다.
 */
interface UserAsyncRepository {
    fun findByIdAsync(id: Long): CompletableFuture<String>
}

class CoroutineBackedUserAsyncRepository : UserAsyncRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private suspend fun loadFromDb(id: Long): String {
        delay(50) // 비동기 IO 흉내
        return "user-$id"
    }

    override fun findByIdAsync(id: Long): CompletableFuture<String> {
        return scope.future {
            loadFromDb(id)
        }
    }
}

fun main() {
    val log = LoggerFactory.getLogger(CoroutineBackedUserAsyncRepository::class.java)
    val repo = CoroutineBackedUserAsyncRepository()

    repo.findByIdAsync(42L).thenAccept { user ->
        log.info("loaded: {}", user)
    }
    Thread.sleep(200)
}
