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
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.properties.Delegates

/**
 * 주문 생성 비동기 코드에 CPS 적용하기 - "수작업 코루틴" 의 2단계
 *
 * Upgrade1 의 OrderAsyncExampleUpgrade1 에서 남아있던 문제
 *      1. 각 case 에서 "cont.result = <비동기 결과>" 후 "execute(...)" 재귀 호출" 이 반복된다.
 *          비동기 callback 마다 같은 보일러플레이트가 등장한다.
 *      2. execute 가 자기 자신을 직접 재귀 호출하기 때문에 "다음에 할 일" 을 외부로 분리하기 힘들다.
 *      3. main 에서는 Shared 를 직접 만들지 않으므로, 최종 결과 log 가 execute 의 마지막 case 에 하드코딩되어 있다.
 *
 * 방향
 *      sub4 의 FsmCalculatorUpgrade2 와 동일한 접근이다.
 *          Shared 를 Continuation 구현체(CustomContinuation) 로 바꾸고
 *          비동기 callback 에는 cont::resume 만 넘긴다.
 *      그러면 비동기 결과가 준비됐을 때 프레임워크가 우리가 넘긴 resumeWith 를 호출해주고,
 *          resumeWith 안에서 result 를 갱신하고 execute 를 이어서 부르면 된다. (재귀와 동일한 효과)
 *      마지막 state 에서는 log 대신 main 이 넘긴 completion 을 호출하여 제어를 바깥으로 넘긴다.
 *
 * CustomContinuation
 *      Continuation<Any> 를 구현한다.
 *      main 이 전달한 completion 과 FSM 을 구동할 인스턴스(that) 를 보관한다.
 *      기존 Shared 의 역할(label, result) 뿐만 아니라
 *          원래 execute 의 arguments (userId, productIds) 도 함께 들고 있는다.
 *          -> 비동기 callback (cont::resume) 이 실행된 시점에 arguments 가 유실되지 않도록 보관하는 것이 핵심이다.
 *          -> userId 는 primitive 라서 lateinit 이 안되므로 Delegates.notNull 을 쓴다.
 *      context 는 completion.context 를 그대로 노출한다. (main 에서 EmptyCoroutineContext 를 넣어 만들었다.)
 *      resumeWith 는
 *          result 를 자신의 필드에 저장하고
 *          that.execute(0, emptyList(), this) 를 호출한다.
 *              -> userId, productIds 는 이미 this 에 들어있으므로 execute 쪽에서 this 의 필드를 사용한다.
 *              -> 여기서 전달되는 0, emptyList() 는 쓰이지 않는 플레이스홀더이다.
 *      complete(value) 는 completion.resume(value) 를 호출해 전체 흐름을 종료시킨다.
 *      private class 로 선언하여 외부에서 직접 만들 수 없다.
 *          -> execute 로 들어온 continuation 이 CustomContinuation 인지 여부로
 *             "최초 진입인지 / 재귀 진입인지" 를 구분할 수 있다. (sub4 와 동일)
 *
 * execute 흐름
 *      continuation 이 CustomContinuation 이면 그대로 사용하고,
 *      아니라면 main 이 전달한 completion 이므로 CustomContinuation 으로 감싸고 instance / arguments 를 채운다.
 *      그 뒤 when (cont.label) 로 분기하는 것은 FSM 때와 같다.
 *      다만 각 case 의 비동기 호출 결과를 받는 부분을 람다에서 "cont::resume" 으로 바꾼다.
 *          thenAccept(cont::resume)       // Consumer<Customer>
 *          .subscribe(cont::resume)       // Consumer<List<Product>>
 *          .with(cont::resume)            // Consumer<List<Store>>
 *          .subscribe(FirstFinder(cont::resume)) // (DeliveryAddress) -> Unit
 *          .subscribe(cont::resume)       // Consumer<Order>
 *      여기서 cont::resume 은 "Continuation 의 resume 확장 함수" 를 method reference 로 넘긴 것이다.
 *          resume(value) 는 내부적으로 resumeWith(Result.success(value)) 를 호출한다. (kotlin.coroutines)
 *          즉 비동기 결과가 준비되면 프레임워크가 우리 resumeWith 를 호출해주므로
 *              resumeWith 에서 result 갱신 + that.execute(...) 재진입 이 자동으로 일어난다.
 *      마지막 state (5) 에서는 log 하드코딩 대신 cont.complete(order) 를 호출하여
 *          main 이 넘긴 completion 으로 최종 결과를 전달하고 흐름을 종료한다.
 *
 * main 측 변화
 *      execute 에 "모든 작업이 끝난 뒤 실행할 continuation" 을 직접 만들어 넘긴다.
 *      Continuation(context, lambda) 팩토리 함수로 손쉽게 만들 수 있다.
 *      이 continuation 은 CustomContinuation 이 아니므로, 최초 진입 시 CustomContinuation 으로 감싸진다.
 *
 * 스레드 효율 관점 (왜 이 구조가 중요한가)
 *      findCustomerFuture / findAllProductsFlowable 등 서비스 함수는 내부적으로 non-blocking IO 로 동작한다고 가정해보자.
 *          (학습 코드의 구현체는 Thread.sleep 으로 흉내만 내고 있지만,
 *           실제 운영 코드에선 Netty / Reactor-Netty 같은 진짜 non-blocking IO 가 그 자리에 올것이다.)
 *
 *      1. execute 호출 스레드는 블로킹되지 않는다.
 *          각 case 는 callback (cont::resume) 만 등록하고 when 블록을 즉시 빠져나와 execute 가 return 된다.
 *          호출한 스레드는 다른 요청을 처리하러 자유롭게 떠날 수 있다.
 *      2. 다음 label 은 "비동기 결과를 emit 하는 스레드" 위에서 실행된다.
 *          thenAccept(cont::resume)        -> CompletableFuture 를 완료시킨 스레드 (예: ForkJoinPool)
 *          .subscribe(cont::resume)        -> Flowable 이 emit 하는 스레드
 *          .with(cont::resume)             -> Mutiny 가 emit 하는 스레드
 *          FirstFinder(cont::resume)       -> Publisher 가 onNext 를 호출한 스레드
 *          .subscribe(cont::resume)        -> Mono 가 emit 하는 스레드
 *          -> label 마다 실행 스레드가 바뀔 수 있다. "스레드를 묶어두지 않는" 비동기의 특성이다.
 *      3. 결과적으로 "결과를 기다리느라 놀고 있는 스레드" 가 없다.
 *          적은 수의 스레드로 많은 요청을 동시에 처리할 수 있다. (Reactor/WebFlux event loop 과 같은 이유)
 *          이것이 "스레드 효율" 의 실체이다.
 *
 *      참고: sub5.p4_coroutine 의 suspend 함수 버전에선 "어느 스레드에서 resume 할지" 를
 *          Dispatchers.IO / withContext 로 제어할 수 있다. kotlinx.coroutines 가 이 기계 위에
 *          Dispatcher 라는 한 겹을 더 얹어주는 것이다.
 *          sub6 의 수작업 버전에는 Dispatcher 가 없어서 "resume 스레드 = 비동기 프레임워크가 emit 한 스레드" 로 그대로 노출된다.
 *
 * 구조의 의미
 *      이 구조가 바로 Kotlin compiler 가 suspend 함수를 변환할 때 사용하는 기본 골격이다.
 *          우리가 작성한 선형적인 suspend 코드는
 *          (FSM + CPS + CustomContinuation) 형태로 변환되어 여러 비동기 경계를 넘나든다.
 *      sub5.p4_coroutine 의 OrderCoroutineExample 에서 보았던 "await 이 섞인 선형 코드" 도
 *          결국 컴파일 시 여기에서 수작업으로 만든 것과 같은 모양이 된다.
 *          -> sub6 는 여기서 끝. 이제 sub5.p4_coroutine 를 다시 보면, 라이브러리가 무엇을 해주고 있는지.. 보일수도..?
 */
private val log = KotlinLogging.logger {}

class OrderAsyncExampleUpgrade2(
    private val customerService: CustomerFutureService,
    private val productService: ProductRxjava3Service,
    private val storeService: StoreMutinyService,
    private val deliveryAddressService: DeliveryAddressPublisherService,
    private val orderService: OrderReactorService,
) {

    private class CustomContinuation(
        private val completion: Continuation<Any>, // main 이 넘긴, 가장 마지막에 호출될 continuation
    ) : Continuation<Any> {

        var result: Any? = null
        var label = 0

        // arguments and instance
        //      비동기 callback 이 실행되는 시점에도 execute 의 재진입에 필요한 정보를 유지하기 위해 Continuation 에 싣는다.
        lateinit var that: OrderAsyncExampleUpgrade2
        var userId: Long by Delegates.notNull()
        lateinit var productIds: List<Long>

        // variables
        //      마지막 case 에서 한꺼번에 필요한 중간 값들. (Shared 시절과 동일)
        lateinit var customer: Customer
        lateinit var products: List<Product>
        lateinit var stores: List<Store>
        lateinit var deliveryAddress: DeliveryAddress

        override val context: CoroutineContext
            get() = completion.context

        override fun resumeWith(result: Result<Any>) {
            // 비동기 결과가 준비되면 프레임워크가 여기를 호출해준다.
            this.result = result.getOrThrow()

            // that.execute 를 다시 호출하면서 this (CustomContinuation) 를 함께 넘긴다.
            //      execute 안에서 this 의 label 에 따라 다음 case 가 수행된다.
            //      userId, productIds 는 this 에서 꺼내쓰므로 여기서 전달하는 0, emptyList() 는 의미 없는 플레이스홀더다.
            that.execute(0, emptyList(), this)
        }

        // 마지막 state 에서 호출한다. main 이 넘긴 completion 으로 제어를 넘겨 전체 흐름을 종료시킨다.
        fun complete(value: Any) {
            completion.resume(value)
        }
    }

    fun execute(
        userId: Long,
        productIds: List<Long>,
        continuation: Continuation<Any>,
    ) {
        // 최초 진입이면 main 이 넘긴 completion 을 CustomContinuation 으로 감싸 instance / arguments 를 채운다.
        // 재귀 진입이면 이미 CustomContinuation 이므로 그대로 사용한다.
        //      (CustomContinuation 은 private 이므로 외부에서 넘어온 continuation 은 절대 CustomContinuation 일 수 없다.)
        val cont = if (continuation is CustomContinuation) {
            continuation
        } else {
            CustomContinuation(continuation).apply {
                that = this@OrderAsyncExampleUpgrade2
                this.userId = userId
                this.productIds = productIds
            }
        }

        when (cont.label) {
            0 -> {
                // 1. 고객 정보 조회
                cont.label = 1

                // 비동기 callback 에 cont::resume 만 넘긴다.
                //      findCustomerFuture 의 결과가 준비되면 thenAccept 가 cont::resume 을 호출하고,
                //      resumeWith 가 result 를 저장한 뒤 execute 를 label=1 로 재진입시킨다.
                customerService.findCustomerFuture(cont.userId)
                    .thenAccept(cont::resume)
            }

            1 -> {
                // 2. 상품 정보 조회
                cont.customer = cont.result as Customer
                cont.label = 2

                productService.findAllProductsFlowable(cont.productIds)
                    .toList()
                    .subscribe(cont::resume)
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
                    .with(cont::resume)
            }

            3 -> {
                // 4. 주소 조회
                cont.stores = cont.result as List<Store>
                cont.label = 4

                val customer = cont.customer
                val daIds = customer.deliveryAddressIds
                deliveryAddressService.findDeliveryAddressesPublisher(daIds)
                    .subscribe(FirstFinder(cont::resume))
            }

            4 -> {
                // 5. 주문 생성
                cont.deliveryAddress = cont.result as DeliveryAddress
                cont.label = 5

                val customer = cont.customer
                val products = cont.products
                val deliveryAddress = cont.deliveryAddress
                val stores = cont.stores

                orderService.createOrderMono(
                    customer, products, deliveryAddress, stores,
                ).subscribe(cont::resume)
            }

            5 -> {
                // log 하드코딩 대신, main 이 넘긴 completion 으로 최종 결과를 전달하고 흐름을 종료한다.
                val order = cont.result as Order
                cont.complete(order)
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

    val example = OrderAsyncExampleUpgrade2(
        customerService = customerService,
        productService = productService,
        storeService = storeService,
        deliveryAddressService = deliveryAddressService,
        orderService = orderService,
    )

    // "모든 작업이 끝난 뒤 실행할 continuation" 을 main 에서 직접 만든다.
    //      Continuation(context, lambda) 팩토리로 Continuation 인터페이스를 간편하게 구현할 수 있다.
    //      이 continuation 은 CustomContinuation 이 아니므로 execute 내부에서 감싸진다.
    val cont = Continuation<Any>(EmptyCoroutineContext) {
        log.info { "result: ${it.getOrThrow()}" }
    }

    example.execute(1L, listOf(1L, 2L, 3L), cont)

    // 비동기 작업이 관찰되도록 잠시 대기한다. (학습 목적의 단순 대기)
    Thread.sleep(5000)
}
