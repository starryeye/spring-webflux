package dev.starryeye.coroutine_basic.sub8_coroutine_converted_by_kotlin_compiler

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Order
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.CustomerFutureService
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.DeliveryAddressPublisherService
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.OrderReactorService
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.ProductRxjava3Service
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.StoreMutinyService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlin.system.measureTimeMillis

/**
 * 결국 Kotlin compiler 는 아래와 같은 일을 해준다.
 * 1. suspend 함수에 Continuation 인자를 추가한다.
 * 2. 지역 변수와 다음 재개 지점을 저장할 Continuation 구현체를 생성한다.
 * 3. suspend point(await 등)마다 label 기반 분기문을 둔 state machine 형태로 바꾼다. (switch/when 문)
 * 4. suspend point 에서는 현재 상태를 저장하고 COROUTINE_SUSPENDED 를 반환한다.
 * 5. 비동기 작업이 끝나면 continuation.resumeWith(...) 로 다음 상태부터 재개한다.
 * 6. 마지막에는 최종 결과(Order)를 반환하거나 예외를 전파한다.
 *
 * 즉, 아래 코드는 Kotlin compiler 를 거치면 sub6.OrderAsyncExampleUpgrade2 처럼
 *      "상태를 저장했다가 나중에 이어서 실행할 수 있는 형태" 로 풀린다.
 *
 * Coroutine
 *      Kotlin compiler 에 의해 결국 아래 코드는 FSM, CPS 가 적용된다.
 *          코드 흐름을 일시 중단하고 재개 가능해질 것이다.
 *      코드 흐름을 일시 중단하고 재개 가능한 단위를 coroutine 이라 한다.
 *      Coroutine 을 사용하면 비동기 영역에서 결과가 반환될 때 까지 일시 중단(suspend)하고 결과가 반환되면 재개(resume)한다.
 */
private val log = KotlinLogging.logger {}

class OrderCoroutineConversionExample(
    private val customerService: CustomerFutureService,
    private val productService: ProductRxjava3Service,
    private val storeService: StoreMutinyService,
    private val deliveryAddressService: DeliveryAddressPublisherService,
    private val orderService: OrderReactorService,
) {

    suspend fun execute(userId: Long, productIds: List<Long>): Order {

        // 아래 주석은 현재 환경이 “WebFlux event-loop 기반 논블로킹 I/O라고 가정한 비유”


        // eventLoop-1: execute 시작
        val customer = customerService.findCustomerFuture(userId) // eventLoop-1: 고객 조회 논블로킹 I/O를 등록해둘게.
            .await() // eventLoop-1: 응답 올 때까지 난 빠져서 다른 요청 볼게; 커널이 multiplexing 으로 감시하다가 완료 이벤트가 오면 어떤 event loop 스레드(eventLoop-2)가 continuation.resume() 한다.

        val products = productService.findAllProductsFlowable(productIds) // eventLoop-2: 고객 응답으로 깨어난 내가 상품 조회 I/O를 다시 등록한다.
            .toList() // eventLoop-2: 흘러오는 상품들을 List 로 모아 한 번에 넘길 준비를 한다.
            .await() // eventLoop-2: 상품이 다 올 때까지 또 빠진다; 완료 이벤트를 받은 event loop 스레드(eventLoop-3)가 이 다음 상태부터 이어간다.

        val storeIds = products.map { it.storeId } // eventLoop-3: 다시 깨어난 내가 메모리에 있던 값으로 storeIds 를 바로 계산한다.
        val stores = storeService.findStoresMulti(storeIds) // eventLoop-3: 스토어 조회 논블로킹 I/O를 등록한다.
            .collect() // eventLoop-3: 여러 응답 조각을 수집할 파이프를 만든다.
            .asList() // eventLoop-3: 수집 결과를 List 로 정리하겠다고 약속한다.
            .awaitSuspending() // eventLoop-3: 스토어 응답 기다리는 동안 난 또 다른 일 하러 간다; eventLoop-4 스레드가 continuation.resume() 을 호출하여 아래 코드부터 이어나갈 수 있다.

        val deliveryAddress = deliveryAddressService // eventLoop-4: 스토어 응답으로 다시 살아난 내가 배송지 단계로 진입한다.
            .findDeliveryAddressesPublisher(customer.deliveryAddressIds) // eventLoop-4: 배송지 조회 논블로킹 I/O를 등록한다.
            .awaitFirst() // eventLoop-4: 첫 응답이 올 때까지 빠진다; 응답 이벤트를 잡은 event loop 스레드(eventLoop-5)가 이후 코드를 수행한다.

        return orderService.createOrderMono( // eventLoop-5: 배송지 응답 이후 재개된 내가 주문 생성 I/O를 등록한다.
            customer = customer, // eventLoop-5: suspend 전 지역 변수 상태가 복원돼서 그대로 넘긴다.
            products = products, // eventLoop-5: compiler 가 저장해둔 코루틴 상태 덕분에 값이 안 사라진다.
            deliveryAddress = deliveryAddress, // eventLoop-5: 이어달리기하듯 앞 단계 결과를 그대로 사용한다.
            stores = stores, // eventLoop-5: 이전 await 들을 지나며 모아둔 값도 그대로 살아있다.
        ).awaitSingle() // eventLoop-5: 마지막으로 빠졌다가 완료 이벤트를 받은 스레드가 resume 하면, FSM 이 다음 상태로 가서 Order 를 반환하고 execute 가 끝난다.
    }
}

fun main() = runBlocking {
    val example = OrderCoroutineConversionExample(
        customerService = CustomerFutureService(),
        productService = ProductRxjava3Service(),
        storeService = StoreMutinyService(),
        deliveryAddressService = DeliveryAddressPublisherService(),
        orderService = OrderReactorService(),
    )

    val elapsed = measureTimeMillis {
        val order = example.execute(1L, listOf(1L, 2L, 3L))
        log.info { "order: $order" }
    }
    log.info { "elapsed: ${elapsed}ms" }
}
