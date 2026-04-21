package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Product
import reactor.core.publisher.Flux

class ProductReactorService {

    fun findAllProductsFlux(ids: List<Long>): Flux<Product> {
        return Flux.create { sink ->
            ids.forEach { id ->
                Thread.sleep(100)
                sink.next(Product(id = id, name = "상품 $id", price = 1000L + id))
            }
            sink.complete()
        }
    }
}
