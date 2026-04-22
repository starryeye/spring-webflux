package dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Customer
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.DeliveryAddress
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Order
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Product
import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Store
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.CustomerFutureService
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.DeliveryAddressPublisherService
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.FirstFinder
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.OrderReactorService
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.ProductRxjava3Service
import dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service.StoreMutinyService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * 주문 생성 비동기 코드에 FSM 적용하기 - "수작업 코루틴" 의 1단계
 *
 * 이 sub 의 포지션
 *      sub5.p4_coroutine 의 OrderCoroutineExample 은 suspend + .await() 로 작성된 "완성본" 이다.
 *          kotlinx.coroutines 라이브러리가 FSM + CPS + Continuation 생성을 전부 대신 해준다.
 *          우리는 선형 코드만 쓰면, 컴파일러/런타임이 상태 분기와 재진입 로직까지 만들어 돌려준다.
 *      sub6 에서는 그 라이브러리의 혜택을 쓰지 않고, 같은 결과를 "직접 손으로" 만들어본다.
 *          이 파일(Upgrade1) 에서는 먼저 FSM 만 적용해 중첩을 펴고,
 *          Upgrade2 에서 Continuation 까지 얹는다.
 *          완성하고 나면 sub5.p4_coroutine 가 내부적으로 어떤 모양인지 감이 잡힌다.
 *
 * 코드의 출발점은 sub5.p2_async 의 OrderAsyncExample
 *      이 파일의 execute 는 sub5.p2_async 를 그대로 가져와서 FSM 으로 뒤집은 것이다.
 *          동일한 시나리오 / 동일한 비동기 라이브러리 혼합이다. 바뀐 것은 "조립 방식" 뿐이다.
 *
 * 목표
 *      sub2 의 FSM 을 비동기 코드에도 적용해서 중첩 구조를 제거해본다.
 *          각 단계를 label 로 구분되는 case 로 분리하고,
 *          비동기 결과가 준비되면 Shared 에 저장한 뒤 execute 를 재귀 호출하여 다음 case 로 transition 한다.
 *
 * Shared
 *      sub2 의 State 와 같은 역할이다. label (현재 상태) 과 result (직전 case 의 비동기 결과) 를 들고 있다.
 *      다만 여기서는 한 번에 한 값만 쓰이던 sub2 와 달리
 *          마지막 case (주문 생성) 에서 customer, products, stores, deliveryAddress 가 모두 필요하다.
 *      따라서 중간 값들을 버리지 않고 따로 보관하는 필드를 추가한다. (lateinit var 로 선언)
 *
 * execute 흐름
 *      shared 를 인자로 받고 기본값은 null 로 둔다.
 *          main 에서 최초 호출될 때는 null 이 전달되므로 Shared 객체를 새로 생성한다.
 *          재귀 호출될 때는 이미 만들어진 Shared 가 전달되므로 그대로 사용한다.
 *      label 에 따라 when 으로 분기하여 해당 비동기 호출을 수행한다.
 *      각 case 의 끝에서
 *          이전 case 의 result 를 꺼내 Shared 의 중간 값 필드에 저장하고
 *          label 을 다음 값으로 바꾼 뒤
 *          비동기 호출의 callback (thenAccept / subscribe / ...) 안에서
 *              cont.result = <비동기 결과>
 *              execute(userId, productIds, cont) // 재귀 호출로 transition
 *          를 수행한다.
 *      마지막 case (5) 에서는 result 를 Order 로 꺼내 log 로 출력한다.
 *
 * 관찰
 *      subscribe 가 중첩되지 않는다. 각 비동기 호출은 자기 case 안에서만 등장한다.
 *      바깥 scope 변수 의존도 없어졌다. 모든 중간 값은 Shared 에 담긴다.
 *      대신 "재귀 호출" 과 "label 수동 관리" 라는 비용이 생겼다.
 *          이 비용은 sub4 에서처럼 CPS 를 얹어 Continuation 형태로 바꾸면 한 단계 더 정리된다.
 *          -> 다음 단계: 같은 sub6 의 OrderAsyncExampleUpgrade2
 */
private val log = KotlinLogging.logger {}

class OrderAsyncExampleUpgrade1(
    private val customerService: CustomerFutureService,
    private val productService: ProductRxjava3Service,
    private val storeService: StoreMutinyService,
    private val deliveryAddressService: DeliveryAddressPublisherService,
    private val orderService: OrderReactorService,
) {

    class Shared {
        var result: Any? = null // 직전 case 의 비동기 결과가 잠깐 머무는 자리
        var label = 0           // 현재 상태

        // variables
        //      마지막 case (주문 생성) 에서 한꺼번에 필요하므로 중간 값들을 따로 보관한다.
        lateinit var customer: Customer
        lateinit var products: List<Product>
        lateinit var stores: List<Store>
        lateinit var deliveryAddress: DeliveryAddress
    }

    fun execute(userId: Long, productIds: List<Long>, shared: Shared? = null) {
        // 최초 진입이면 Shared 를 새로 만들고, 재귀 진입이면 전달받은 Shared 를 그대로 사용한다.
        val cont = shared ?: Shared()

        when (cont.label) {
            0 -> {
                // 1. 고객 정보 조회
                cont.label = 1

                customerService.findCustomerFuture(userId)
                    .thenAccept { customer ->
                        cont.result = customer
                        execute(userId, productIds, cont) // 재귀 호출로 다음 state 로 transition
                    }
            }

            1 -> {
                // 2. 상품 정보 조회
                cont.customer = cont.result as Customer // 직전 result 를 중간 값으로 옮겨 보관
                cont.label = 2

                productService.findAllProductsFlowable(productIds)
                    .toList()
                    .subscribe { products ->
                        cont.result = products
                        execute(userId, productIds, cont)
                    }
            }

            2 -> {
                // 3. 스토어 조회
                cont.products = cont.result as List<Product>
                cont.label = 3

                val products = cont.products
                val storeIds = products.map { it.storeId }
                storeService.findStoresMulti(storeIds)
                    .collect().asList()
                    .subscribe()
                    .with { stores ->
                        cont.result = stores
                        execute(userId, productIds, cont)
                    }
            }

            3 -> {
                // 4. 주소 조회
                cont.stores = cont.result as List<Store>
                cont.label = 4

                val customer = cont.customer
                val daIds = customer.deliveryAddressIds
                deliveryAddressService.findDeliveryAddressesPublisher(daIds)
                    .subscribe(FirstFinder { deliveryAddress ->
                        cont.result = deliveryAddress
                        execute(userId, productIds, cont)
                    })
            }

            4 -> {
                // 5. 주문 생성
                cont.deliveryAddress = cont.result as DeliveryAddress
                cont.label = 5

                // 그동안 보관해둔 모든 중간 값들을 Shared 에서 꺼내 사용한다. (바깥 scope 의존 X)
                val customer = cont.customer
                val products = cont.products
                val deliveryAddress = cont.deliveryAddress
                val stores = cont.stores

                orderService.createOrderMono(
                    customer, products, deliveryAddress, stores,
                ).subscribe { order ->
                    cont.result = order
                    execute(userId, productIds, cont)
                }
            }

            5 -> {
                // 결과 출력 (sub2 의 마지막 state 와 동일하게, 여기서 log 가 하드코딩 되어 있다 -> CPS 에서 제거)
                val order = cont.result as Order
                log.info { "order: $order" }
            }
        }
    }
}

fun main() {
    val customerService = CustomerFutureService()
    val productService = ProductRxjava3Service()
    val storeService = StoreMutinyService()
    val deliveryAddressService = DeliveryAddressPublisherService()
    val orderService = OrderReactorService()

    val example = OrderAsyncExampleUpgrade1(
        customerService = customerService,
        productService = productService,
        storeService = storeService,
        deliveryAddressService = deliveryAddressService,
        orderService = orderService,
    )

    // 최초 호출은 shared = null 로 들어간다. 이후 비동기 callback 에서 재귀 호출로 state 가 진행된다.
    example.execute(1L, listOf(1L, 2L, 3L))

    // main 스레드가 바로 끝나면 비동기 작업이 관찰되지 않으므로 잠깐 대기한다. (학습 목적의 단순 대기)
    Thread.sleep(5000)
}
