package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Customer
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.DeliveryAddress
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Order
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Product
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Store

class OrderBlockingService {

    fun createOrder(
        customer: Customer,
        products: List<Product>,
        deliveryAddress: DeliveryAddress,
        stores: List<Store>,
    ): Order {
        Thread.sleep(500)
        return Order(
            customer = customer,
            products = products,
            stores = stores,
            deliveryAddress = deliveryAddress,
        )
    }
}
