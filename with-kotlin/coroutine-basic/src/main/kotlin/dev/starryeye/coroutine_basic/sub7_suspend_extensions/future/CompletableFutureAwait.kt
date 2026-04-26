package dev.starryeye.coroutine_basic.sub7_suspend_extensions.future

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis

/**
 * CompletableFuture / CompletionStage 용 suspend 확장 함수
 *
 * sub5.p4 의 첫 줄
 *      customerService.findCustomerFuture(userId).await()
 *
 * package
 *      kotlinx.coroutines.future
 *
 * 핵심 시그니처
 *      suspend fun <T> CompletionStage<T>.await(): T
 *
 * 의미
 *      future 완료까지 코루틴만 suspend 하고, 완료되면 값을 돌려준다.
 *      get() 과 달리 호출 스레드를 block 하지 않는다는 점이 핵심이다.
 *      실패한 future 는 await 지점에서 예외로 다시 던져진다.
 *
 * 언제 쓰나
 *      JDK CompletableFuture / CompletionStage 를 반환하는 API 를 코루틴에서 값처럼 읽고 싶을 때 쓴다.
 *
 * 참고.
 *      - CompletionStage<T>.asDeferred()
 *      - Deferred<T>.asCompletableFuture()
 */
private val log = KotlinLogging.logger {}

/**
 * 학습용으로 Thread.sleep 으로 IO 를 흉내낸다.
 * 실제 운영 코드에선 non-blocking IO (HttpClient.sendAsync / WebClient 등) 가 이 자리에 온다.
 */
private fun fetchUserNameFuture(userId: Long): CompletableFuture<String> =
    CompletableFuture.supplyAsync {
        Thread.sleep(300)
        "user-$userId"
    }

fun main() = runBlocking {
    val elapsed = measureTimeMillis {
        // 1) 기본: CompletableFuture<T>.await() -> T
        val name: String = fetchUserNameFuture(1L).await()
        log.info { "name: $name" }

        // 2) 이미 완료된 future 는 바로 resume 된다.
        val immediate = CompletableFuture.completedFuture(42).await()
        log.info { "immediate: $immediate" }

        // 3) 실패한 future 는 await 지점에서 예외로 다시 보인다.
        val failed = CompletableFuture<String>().apply {
            completeExceptionally(IllegalStateException("boom"))
        }
        runCatching { failed.await() }
            .onFailure { log.info { "caught: ${it::class.simpleName} - ${it.message}" } }
    }
    log.info { "elapsed: ${elapsed}ms" }
}
