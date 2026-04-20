package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Order
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync.service.CustomerBlockingService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync.service.DeliveryAddressBlockingService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync.service.OrderBlockingService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync.service.ProductBlockingService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync.service.StoreBlockingService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.measureTimeMillis

/**
 * 주문 생성 - 동기(Blocking) 코드
 *
 * 구매자의 userId 와 구매하려는 상품 productIds 가 주어졌을 때
 *      1. userId 로 고객 정보를 조회
 *      2. productIds 로 상품 정보를 조회
 *      3. 상품 정보로 스토어 정보를 조회
 *      4. 고객 정보로 배송지 주소를 조회
 *      5. 이 모든 값들을 활용하여 주문 생성
 *
 * 모든 단계가 값을 곧바로 반환(return) 하는 direct style 이다.
 *      코드 흐름이 위->아래로 선형이어서 읽기 쉽다.
 *      다만, 각 단계에서 스레드를 블로킹하므로 전체 처리 시간이 길어진다.
 */
private val log = KotlinLogging.logger {}

class OrderBlockingExample(
    private val customerService: CustomerBlockingService,
    private val productService: ProductBlockingService,
    private val storeService: StoreBlockingService,
    private val deliveryAddressService: DeliveryAddressBlockingService,
    private val orderService: OrderBlockingService,
) {

    fun execute(userId: Long, productIds: List<Long>): Order {
        // 1. 고객 정보 조회
        val customer = customerService.findCustomerById(userId)

        // 2. 상품 정보 조회
        val products = productService.findAllProductsByIds(productIds)

        // 3. 스토어 조회
        val storeIds = products.map { it.storeId }
        val stores = storeService.findStoresByIds(storeIds)

        // 4. 주소 조회
        val daIds = customer.deliveryAddressIds
        val deliveryAddress = deliveryAddressService.findDeliveryAddresses(daIds).first()

        // 5. 주문 생성
        val order = orderService.createOrder(
            customer, products, deliveryAddress, stores,
        )

        return order
    }
}

fun main() {
    val example = OrderBlockingExample(
        customerService = CustomerBlockingService(),
        productService = ProductBlockingService(),
        storeService = StoreBlockingService(),
        deliveryAddressService = DeliveryAddressBlockingService(),
        orderService = OrderBlockingService(),
    )

    // 네 패키지(p1~p4) 모두 "같은 시나리오" 를 각자의 스타일로 표현한 것이라, elapsed 는 모두 ~2900ms 로 비슷하다.
    //      여기서의 관심사는 성능이 아니라 코드의 모양(style) 비교이다.
    //      대기 시간을 겹쳐서 줄이는 병렬화(Reactor subscribeOn / Coroutine async{}) 는 별도 주제로 다룬다.
    val elapsed = measureTimeMillis {
        val order = example.execute(1L, listOf(1L, 2L, 3L))
        log.info { "order: $order" }
    }
    log.info { "elapsed: ${elapsed}ms" }
}
