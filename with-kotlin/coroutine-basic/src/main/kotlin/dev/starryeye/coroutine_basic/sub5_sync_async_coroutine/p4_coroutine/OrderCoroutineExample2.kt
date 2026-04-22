package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Order
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine.service.CustomerFutureService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine.service.DeliveryAddressPublisherService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine.service.OrderReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine.service.ProductRxjava3Service
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine.service.StoreMutinyService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * 주문 생성 - Coroutine + 진짜 병렬화 (Example2)
 *
 * OrderCoroutineExample 은 .await() 을 순차로 호출했기 때문에 각 단계가 선형 실행되어 elapsed ≈ 1700ms 였다.
 *
 * 여기서는 의존 없는 호출들을 async { } 로 감싸서 동시에 시작시킨다.
 *      async 는 coroutineScope 내에서 Deferred<T> 를 반환하며, 내부 블록을 별도의 코루틴으로 실행한다.
 *      Deferred.await() 로 결과를 받을 때 두 async 가 이미 병렬로 진행되고 있다.
 *      이론 최소: max(300, 300) + max(300, 300) + 500 ≈ 1100ms
 *
 * Dispatchers.IO 를 쓰는 이유
 *      CompletableFuture 는 supplyAsync 로 자체 스레드(ForkJoinPool) 에서 일하므로 dispatcher 무관하지만,
 *      Flowable/Multi/Publisher/Mono 의 create { Thread.sleep } 은 "subscribe 하는 스레드" 를 블로킹 한다.
 *      runBlocking 의 기본 dispatcher 는 단일 스레드라 그대로 두면 async 들이 한 스레드를 공유하며 serialize 된다.
 *      async(Dispatchers.IO) 로 각 블록을 IO 스레드풀에 디스패치해야 실제로 병렬 실행된다.
 *
 * 관찰
 *      p3 의 subscribeOn 과 개념상 동일하다. "어느 스레드에서 일할지" 를 async 바인딩에서 지정.
 *      선형 흐름(val x = ...await()) 을 유지하면서 병렬화 지점을 async{}.await() 로 표현 가능.
 *      p1/p2 의 수동 조립과 비교하면 훨씬 간결하다.
 */
private val log = KotlinLogging.logger {}

class OrderCoroutineExample2(
    private val customerService: CustomerFutureService,
    private val productService: ProductRxjava3Service,
    private val storeService: StoreMutinyService,
    private val deliveryAddressService: DeliveryAddressPublisherService,
    private val orderService: OrderReactorService,
) {

    suspend fun execute(userId: Long, productIds: List<Long>): Order = coroutineScope {
        // 1 & 2. customer + products 병렬
        val customerDeferred = async(Dispatchers.IO) {
            customerService.findCustomerFuture(userId).await()
        }
        val productsDeferred = async(Dispatchers.IO) {
            productService.findAllProductsFlowable(productIds).toList().await()
        }

        val customer = customerDeferred.await()
        val products = productsDeferred.await()

        // 3 & 4. stores + deliveryAddress 병렬
        val storeIds = products.map { it.storeId }
        val storesDeferred = async(Dispatchers.IO) {
            storeService.findStoresMulti(storeIds).collect().asList().awaitSuspending()
        }
        val deliveryAddressDeferred = async(Dispatchers.IO) {
            deliveryAddressService.findDeliveryAddressesPublisher(customer.deliveryAddressIds)
                .awaitFirst()
        }

        val stores = storesDeferred.await()
        val deliveryAddress = deliveryAddressDeferred.await()

        // 5. order 생성 (병렬 기회 없음)
        withContext(Dispatchers.IO) {
            orderService.createOrderMono(customer, products, deliveryAddress, stores)
                .awaitSingle()
        }
    }
}

fun main() = runBlocking {
    val example = OrderCoroutineExample2(
        customerService = CustomerFutureService(),
        productService = ProductRxjava3Service(),
        storeService = StoreMutinyService(),
        deliveryAddressService = DeliveryAddressPublisherService(),
        orderService = OrderReactorService(),
    )

    // async 로 진짜 병렬화 -> elapsed ≈ 1100ms 예상 (기존 Example: ~1700ms)
    val elapsed = measureTimeMillis {
        val order = example.execute(1L, listOf(1L, 2L, 3L))
        log.info { "order: $order" }
    }
    log.info { "elapsed: ${elapsed}ms" }
}
