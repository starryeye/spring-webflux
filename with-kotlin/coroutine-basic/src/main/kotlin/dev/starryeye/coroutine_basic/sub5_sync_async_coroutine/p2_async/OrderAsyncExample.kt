package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p2_async

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p2_async.service.CustomerFutureService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p2_async.service.DeliveryAddressPublisherService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p2_async.service.FirstFinder
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p2_async.service.OrderReactorService
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p2_async.service.ProductRxjava3Service
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p2_async.service.StoreMutinyService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CountDownLatch
import kotlin.system.measureTimeMillis

/**
 * 주문 생성 - 비동기 코드 (subscribe hell)
 *
 * 여러 비동기 라이브러리가 섞여 있다.
 *      CustomerFutureService        -> CompletableFuture
 *      ProductRxjava3Service        -> Flowable (RxJava3)
 *      StoreMutinyService           -> Multi (Mutiny)
 *      DeliveryAddressPublisherService -> Publisher (reactive-streams)
 *      OrderReactorService          -> Mono (Reactor)
 *
 * 문제점
 *      여러 비동기 라이브러리마다 비동기 결과를 consume 하는 방식이 다르다.
 *          thenAccept, subscribe { }, subscribe().with { }, subscribe(Subscriber) ...
 *          그래서, chaining 이 어려웠다..
 *      그래도.. chaining 을 하기 위해선 하나의 값을 받고 하나의 값을 전달하는 패턴을 사용해야 하는데,
 *          createOrder 처럼 "그동안 찾은 모든 값"이 필요한 경우 바깥 scope 의 변수에 의존해야 한다.
 *          -> 결국 subscribe 를 중첩(hell) 하게 된다.
 *
 * 참고
 *      실무에서는 if, for 문등 훨씬 복잡한 상황이 나올 것이다.
 *      그래서 subscribe hell 을 더욱 피하기 힘들다.
 *
 */
private val log = KotlinLogging.logger {}

class OrderAsyncExample(
    private val customerService: CustomerFutureService,
    private val productService: ProductRxjava3Service,
    private val storeService: StoreMutinyService,
    private val deliveryAddressService: DeliveryAddressPublisherService,
    private val orderService: OrderReactorService,
) {

    fun execute(userId: Long, productIds: List<Long>, onComplete: () -> Unit = {}) {
        // 1. 고객 정보 조회
        customerService.findCustomerFuture(userId).thenAccept { customer ->
            // 2. 상품 정보 조회
            productService.findAllProductsFlowable(productIds)
                .toList()
                .subscribe { products ->
                    // 3. 스토어 조회
                    val storeIds = products.map { it.storeId }
                    storeService.findStoresMulti(storeIds)
                        .collect().asList()
                        .subscribe().with { stores ->
                            // 4. 주소 조회
                            val daIds = customer.deliveryAddressIds
                            deliveryAddressService.findDeliveryAddressesPublisher(daIds)
                                .subscribe(FirstFinder { deliveryAddress ->
                                    // 5. 주문 생성
                                    orderService.createOrderMono(
                                        customer, products, deliveryAddress, stores,
                                    ).subscribe { order ->
                                        log.info { "order: $order" }
                                        onComplete()
                                    }
                                })
                        }
                }
        }
    }
}

fun main() {
    val example = OrderAsyncExample(
        customerService = CustomerFutureService(),
        productService = ProductRxjava3Service(),
        storeService = StoreMutinyService(),
        deliveryAddressService = DeliveryAddressPublisherService(),
        orderService = OrderReactorService(),
    )

    // 모든 비동기 작업이 끝날때까지 main 스레드가 살아있도록 latch 로 대기
    val latch = CountDownLatch(1)
    // 네 패키지(p1~p4) 모두 "같은 시나리오" 를 각자의 스타일로 표현한 것이라, elapsed 는 모두 ~1700ms 로 비슷하다.
    //      여기서의 관심사는 성능이 아니라 코드의 모양(style) 비교이다.
    //      대기 시간을 겹쳐서 줄이는 병렬화(Reactor subscribeOn / Coroutine async{}) 는 별도 주제로 다룬다.
    val elapsed = measureTimeMillis {
        example.execute(1L, listOf(1L, 2L, 3L)) { latch.countDown() }
        latch.await()
    }
    log.info { "elapsed: ${elapsed}ms" }
}
