package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Order
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine.service.CustomerFutureService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine.service.DeliveryAddressPublisherService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine.service.OrderReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine.service.ProductRxjava3Service
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p4_coroutine.service.StoreMutinyService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/**
 * 주문 생성 - Coroutine 코드
 *
 * p2_async 와 동일하게 여러 비동기 라이브러리(Future/Flowable/Multi/Publisher/Mono) 가 섞여있다.
 *      그러나 thenAccept, subscribe 가 모두 사라졌다.
 *      각 비동기 타입에 대한 코루틴 확장 함수를 사용하여 결과를 값으로 받는다.
 *          CompletableFuture  -> .await()            (kotlinx-coroutines-jdk8 / core)
 *          Flowable.toList()  -> .await()            (kotlinx-coroutines-rx3)
 *          Mutiny Uni         -> .awaitSuspending()  (mutiny-kotlin)
 *          Publisher          -> .awaitFirst()       (kotlinx-coroutines-reactive)
 *          Mono               -> .awaitSingle()      (kotlinx-coroutines-reactor)
 *
 * 결과적으로 p1_sync 의 동기 코드와 "코드의 모양" 이 거의 같아진다.
 *      execute 가 suspend 함수가 되었을 뿐, 위 -> 아래 선형 흐름이 그대로 유지된다.
 *      subscribe hell 도 없고, 바깥 scope 변수 의존도 없다.
 *      각 await* 호출 지점에서 스레드를 블로킹하지 않고 일시 중단(suspend) 된다.
 *
 * 이것이 앞서 sub4 에서 다룬 "컴파일러가 suspend 를 FSM + CPS + Continuation 으로 변환" 덕분에 가능해진 것이다.
 */
private val log = KotlinLogging.logger {}

class OrderCoroutineExample(
    private val customerService: CustomerFutureService,
    private val productService: ProductRxjava3Service,
    private val storeService: StoreMutinyService,
    private val deliveryAddressService: DeliveryAddressPublisherService,
    private val orderService: OrderReactorService,
) {

    suspend fun execute(userId: Long, productIds: List<Long>): Order {
        // 1. 고객 정보 조회 (CompletableFuture)
        val customer = customerService.findCustomerFuture(userId).await()

        // 2. 상품 정보 조회 (RxJava3 Flowable)
        val products = productService.findAllProductsFlowable(productIds)
            .toList().await()

        // 3. 스토어 조회 (Mutiny Multi -> Uni<List>)
        val storeIds = products.map { it.storeId }
        val stores = storeService.findStoresMulti(storeIds)
            .collect().asList().awaitSuspending()

        // 4. 주소 조회 (reactive-streams Publisher)
        val daIds = customer.deliveryAddressIds
        val deliveryAddress = deliveryAddressService
            .findDeliveryAddressesPublisher(daIds)
            .awaitFirst()

        // 5. 주문 생성 (Reactor Mono)
        val order = orderService.createOrderMono(
            customer, products, deliveryAddress, stores,
        ).awaitSingle()

        return order
    }
}

fun main() = runBlocking {
    val example = OrderCoroutineExample(
        customerService = CustomerFutureService(),
        productService = ProductRxjava3Service(),
        storeService = StoreMutinyService(),
        deliveryAddressService = DeliveryAddressPublisherService(),
        orderService = OrderReactorService(),
    )

    // 네 패키지(p1~p4) 모두 "같은 시나리오" 를 각자의 스타일로 표현한 것이라, elapsed 는 모두 ~1700ms 로 비슷하다.
    //      여기서의 관심사는 성능이 아니라 코드의 모양(style) 비교이다.
    //      대기 시간을 겹쳐서 줄이는 병렬화(Reactor subscribeOn / Coroutine async{}) 는 별도 주제로 다룬다.
    val elapsed = measureTimeMillis {
        val order = example.execute(1L, listOf(1L, 2L, 3L))
        log.info { "order: $order" }
    }
    log.info { "elapsed: ${elapsed}ms" }
}
