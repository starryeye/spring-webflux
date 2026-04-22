package dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Customer
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.DeliveryAddress
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Order
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Product
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Store
import reactor.core.publisher.Mono

/**
 * Order 를 Reactor 의 Mono 로 반환한다.
 */
class OrderReactorService {

    fun createOrderMono(
        customer: Customer,
        products: List<Product>,
        deliveryAddress: DeliveryAddress,
        stores: List<Store>,
    ): Mono<Order> {
        return Mono.create { sink ->
            Thread.sleep(500)
            sink.success(
                Order(
                    customer = customer,
                    products = products,
                    stores = stores,
                    deliveryAddress = deliveryAddress,
                )
            )
        }
    }
}
