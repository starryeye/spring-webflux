package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Customer

class CustomerBlockingService {

    fun findCustomerById(id: Long): Customer {
        Thread.sleep(300) // 네트워크/DB 호출 시뮬레이션
        return Customer(id, "starry", listOf(1L, 2L, 3L))
    }
}
