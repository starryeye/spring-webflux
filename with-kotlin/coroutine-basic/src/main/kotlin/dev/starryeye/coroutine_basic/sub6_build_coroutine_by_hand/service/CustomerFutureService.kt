package dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Customer
import java.util.concurrent.CompletableFuture

/**
 * 고객 정보를 Java8 의 CompletableFuture 로 반환한다.
 */
class CustomerFutureService {

    fun findCustomerFuture(id: Long): CompletableFuture<Customer> {
        return CompletableFuture.supplyAsync {
            Thread.sleep(300)
            Customer(id, "starry", listOf(1L, 2L, 3L))
        }
    }
}
