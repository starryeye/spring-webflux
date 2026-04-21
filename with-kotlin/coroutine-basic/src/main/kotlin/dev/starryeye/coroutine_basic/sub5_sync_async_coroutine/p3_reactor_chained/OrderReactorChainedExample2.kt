package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Order
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service.CustomerReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service.DeliveryAddressReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service.OrderReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service.ProductReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service.StoreReactorService
import io.github.oshai.kotlinlogging.KotlinLogging
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import kotlin.system.measureTimeMillis

/**
 * 주문 생성 - Reactor chaining + 진짜 병렬화 (Example2)
 *
 * OrderReactorChainedExample 은 Mono.zip 으로 체이닝 했지만,
 *      각 Mono 의 Thread.sleep 이 subscribing 스레드를 블로킹 한다.
 *      기본 subscribe 스레드는 하나이므로 실제로는 순차 실행되어 elapsed ≈ 1700ms 였다.
 *
 * 여기서는 각 upstream publisher 에 .subscribeOn(Schedulers.boundedElastic()) 을 추가한다.
 *      subscribeOn 은 "해당 publisher 의 create 람다(= Thread.sleep) 를 어느 스케줄러에서 실행할지" 를 지정한다.
 *      zip 이 두 Mono 를 동시에 구독하면 각자 다른 worker 스레드에서 병렬 실행된다.
 *      이론 최소: max(300, 300) + max(300, 300) + 500 ≈ 1100ms
 *
 * 관찰
 *      코드 구조는 기존 Example 과 거의 같다. (flatMap / zip 체인)
 *      "어디에서 실행할지" 를 지정하는 한 줄(subscribeOn) 이 추가되었을 뿐이다.
 *      p1/p2 의 수동 병렬화에 비해 훨씬 선언적이다.
 */
private val log = KotlinLogging.logger {}

class OrderReactorChainedExample2(
    private val customerService: CustomerReactorService,
    private val productService: ProductReactorService,
    private val storeService: StoreReactorService,
    private val deliveryAddressService: DeliveryAddressReactorService,
    private val orderService: OrderReactorService,
) {

    fun execute(userId: Long, productIds: List<Long>): Mono<Order> {
        val customerMono = customerService.findCustomerMono(userId)
            .subscribeOn(Schedulers.boundedElastic())
        val productsMono = productService.findAllProductsFlux(productIds)
            .subscribeOn(Schedulers.boundedElastic())
            .collectList()

        return Mono.zip(customerMono, productsMono)
            .flatMap { t1 ->
                val customer = t1.t1
                val products = t1.t2

                val storeIds = products.map { it.storeId }
                val storesMono = storeService.findStoresFlux(storeIds)
                    .subscribeOn(Schedulers.boundedElastic())
                    .collectList()
                val deliveryAddressMono = deliveryAddressService
                    .findDeliveryAddressesFlux(customer.deliveryAddressIds)
                    .subscribeOn(Schedulers.boundedElastic())
                    .next()

                Mono.zip(storesMono, deliveryAddressMono)
                    .flatMap { t2 ->
                        val stores = t2.t1
                        val deliveryAddress = t2.t2

                        orderService.createOrderMono(customer, products, deliveryAddress, stores)
                            .subscribeOn(Schedulers.boundedElastic())
                    }
            }
    }
}

fun main() {
    val example = OrderReactorChainedExample2(
        customerService = CustomerReactorService(),
        productService = ProductReactorService(),
        storeService = StoreReactorService(),
        deliveryAddressService = DeliveryAddressReactorService(),
        orderService = OrderReactorService(),
    )

    // subscribeOn 으로 진짜 병렬화 -> elapsed ≈ 1100ms 예상 (기존 Example: ~1700ms)
    val elapsed = measureTimeMillis {
        val order = example.execute(1L, listOf(1L, 2L, 3L)).block()
        log.info { "order: $order" }
    }
    log.info { "elapsed: ${elapsed}ms" }
}
