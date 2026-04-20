package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Product

class ProductBlockingService {

    fun findAllProductsByIds(ids: List<Long>): List<Product> {
        return ids.map { id ->
            Thread.sleep(100)
            Product(id = id, name = "상품 $id", price = 1000L + id)
        }
    }
}
