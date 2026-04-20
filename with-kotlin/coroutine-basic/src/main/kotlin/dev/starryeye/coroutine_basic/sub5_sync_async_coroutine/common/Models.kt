package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common

/**
 * sub5 전역에서 사용할 도메인 모델
 *
 * 시나리오: 구매자가 여러 상품을 주문한다.
 *      1. userId 로 고객 정보를 조회
 *      2. productIds 로 상품 정보를 조회
 *      3. 상품 정보(storeId) 로 스토어 정보를 조회
 *      4. 고객 정보(deliveryAddressIds) 로 배송지 조회
 *      5. 위 정보들로 Order 생성
 *
 * 네개의 패키지(p1~p4)에서 동일 시나리오를 서로 다른 방식(동기/비동기/Reactor chaining/Coroutine)으로 구현한다.
 */

data class Customer(
    val id: Long,
    val name: String,
    val deliveryAddressIds: List<Long>,
)

data class Product(
    val id: Long,
    val name: String,
    val price: Long,
    val storeId: Long = 1L + (id % 3), // 간단히 id 기반 파생
)

data class Store(
    val id: Long,
    val name: String,
)

data class DeliveryAddress(
    val id: Long,
    val roadNameAddress: String,
    val detailAddress: String,
)

data class Order(
    val customer: Customer,
    val products: List<Product>,
    val stores: List<Store>,
    val deliveryAddress: DeliveryAddress,
)
