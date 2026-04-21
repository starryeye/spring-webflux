package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Customer
import reactor.core.publisher.Mono

class CustomerReactorService {

    fun findCustomerMono(id: Long): Mono<Customer> {
        return Mono.create { sink ->
            Thread.sleep(300)
            sink.success(Customer(id, "starry", listOf(1L, 2L, 3L)))
        }
    }
}
