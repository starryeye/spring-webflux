package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Order
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service.CustomerReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service.DeliveryAddressReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service.OrderReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service.ProductReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service.StoreReactorService
import io.github.oshai.kotlinlogging.KotlinLogging
import reactor.core.publisher.Mono
import kotlin.system.measureTimeMillis

/**
 * 주문 생성 - 비동기 코드 (전부 Reactor 로 통일 + chaining)
 *
 * p2_async 의 "여러 비동기 라이브러리 혼재" 를 Reactor (Mono/Flux) 로 통일했다.
 *      Future -> Mono
 *      Flowable, Multi, Publisher -> Flux
 *      Mono 그대로
 *
 * 모든 타입이 같아지면서 chaining 이 가능하다.
 *      두 개의 값을 동시에 기다려야 할 때는 Mono.zip(...) 을 사용한다.
 *      다음 단계로 전달할 때는 flatMap 을 사용한다.
 *
 * 실행 흐름
 *      [병렬] customer 조회 / products 조회
 *         -> [병렬] stores 조회(products 기반) / deliveryAddress 조회(customer 기반)
 *             -> order 생성
 *
 * subscribe hell 을 제거한 대신 연산자(flatMap/zip) 체인으로 흐름이 구성된다.
 *      변수 바깥 scope 의존성이 사라지고, 단계 사이에 명시적으로 값이 흐른다.
 *      에러 전파도 연산자 체인을 따라 자동으로 이뤄진다.
 *
 * 참고.
 *      코드 구성 자체는 여러 스레드를 사용하게 되면 병렬로 실행가능하지만,
 *      현재 코드상 하나의 main 스레드로만 동작하므로 병렬로 실행되지 않는다.
 */
private val log = KotlinLogging.logger {}

class OrderReactorChainedExample(
    private val customerService: CustomerReactorService,
    private val productService: ProductReactorService,
    private val storeService: StoreReactorService,
    private val deliveryAddressService: DeliveryAddressReactorService,
    private val orderService: OrderReactorService,
) {

    fun execute(userId: Long, productIds: List<Long>): Mono<Order> {
        // 1 & 2. 고객 정보 조회 + 상품 정보 조회 (병렬 zip)
        val customerMono = customerService.findCustomerMono(userId)
        val productsMono = productService.findAllProductsFlux(productIds).collectList()

        return Mono.zip(customerMono, productsMono)
            .flatMap { t1 ->
                val customer = t1.t1
                val products = t1.t2

                // 3 & 4. 스토어 조회 + 주소 조회 (병렬 zip)
                val storeIds = products.map { it.storeId }
                val storesMono = storeService.findStoresFlux(storeIds).collectList()
                val deliveryAddressMono = deliveryAddressService
                    .findDeliveryAddressesFlux(customer.deliveryAddressIds)
                    .next() // Flux -> Mono (첫 번째 원소)

                Mono.zip(storesMono, deliveryAddressMono)
                    .flatMap { t2 ->
                        val stores = t2.t1
                        val deliveryAddress = t2.t2

                        // 5. 주문 생성
                        orderService.createOrderMono(customer, products, deliveryAddress, stores)
                    }
            }
    }
}

fun main() {
    val example = OrderReactorChainedExample(
        customerService = CustomerReactorService(),
        productService = ProductReactorService(),
        storeService = StoreReactorService(),
        deliveryAddressService = DeliveryAddressReactorService(),
        orderService = OrderReactorService(),
    )

    // 네 패키지(p1~p4) 모두 "같은 시나리오" 를 각자의 스타일로 표현한 것이라, elapsed 는 모두 ~1700ms 로 비슷하다.
    //      여기서의 관심사는 성능이 아니라 코드의 모양(style) 비교이다.
    //      대기 시간을 겹쳐서 줄이는 병렬화(Reactor subscribeOn / Coroutine async{}) 는 별도 주제로 다룬다.
    val elapsed = measureTimeMillis {
        val order = example.execute(1L, listOf(1L, 2L, 3L)).block()
        log.info { "order: $order" }
    }
    log.info { "elapsed: ${elapsed}ms" }
}
