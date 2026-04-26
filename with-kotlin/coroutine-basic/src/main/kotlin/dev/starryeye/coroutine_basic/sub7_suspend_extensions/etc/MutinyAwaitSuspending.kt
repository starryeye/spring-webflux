package dev.starryeye.coroutine_basic.sub7_suspend_extensions.etc

import io.github.oshai.kotlinlogging.KotlinLogging
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking

/**
 * Mutiny 타입용 suspend 확장 함수
 *
 * sub5.p4 의 세 번째 줄
 *      storeService.findStoresMulti(storeIds).collect().asList().awaitSuspending()
 *
 * package
 *      io.smallrye.mutiny.coroutines
 *
 * 자주 쓰는 대상
 *      - Uni<T>   -> awaitSuspending()
 *      - Multi<T> -> 보통 collect().asList() 로 Uni<List<T>> 로 만든 뒤 awaitSuspending()
 *
 * 포인트
 *      Mutiny 에서는 이름이 `awaitSuspending()` 이지만 역할은 다른 await 와 같다.
 *      값이 하나면 Uni 를 그대로 기다리고, 값이 여러 개면 Multi 를 먼저 모아야 한다.
 */
private val log = KotlinLogging.logger {}

private fun findStoreUni(storeId: Long): Uni<String> =
    Uni.createFrom().item {
        Thread.sleep(150)
        "store-$storeId"
    }

private fun findStoresMulti(storeIds: List<Long>): Multi<String> =
    Multi.createFrom().emitter { emitter ->
        storeIds.distinct().forEach { id ->
            Thread.sleep(80)
            emitter.emit("store-$id")
        }
        emitter.complete()
    }

fun main() = runBlocking {
    val store = findStoreUni(1L).awaitSuspending()
    log.info { "store: $store" }

    val stores = findStoresMulti(listOf(1L, 2L, 2L, 3L))
        .collect().asList().awaitSuspending()
    log.info { "stores: $stores" }
}
