package dev.starryeye.coroutine_basic.sub7_suspend_extensions.etc

import io.github.oshai.kotlinlogging.KotlinLogging
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitSingleOrNull

/**
 * RxJava3 타입용 suspend 확장 함수
 *
 * sub5.p4 의 두 번째 줄
 *      productService.findAllProductsFlowable(productIds).toList().await()
 *
 * package
 *      kotlinx.coroutines.rx3
 *
 * 자주 쓰는 대상
 *      - Single<T>      -> await()
 *      - Maybe<T>       -> awaitSingleOrNull()
 *      - Completable    -> await()
 *      - Flowable<T>    -> 보통 firstOrError().await(), toList().await()
 *
 * 포인트
 *      sub5.p4 에서 실제로 await 한 대상은 Flowable 자체가 아니라
 *      `toList()` 로 바꾼 뒤의 Single<List<T>> 다.
 *      즉 "다중 값 스트림을 먼저 Single 로 바꾼 뒤 await" 하는 패턴이다.
 */
private val log = KotlinLogging.logger {}

private fun findPriceSingle(productId: Long): Single<Long> =
    Single.fromCallable {
        Thread.sleep(150)
        1000L + productId
    }

private fun findCouponMaybe(exists: Boolean): Maybe<String> =
    if (exists) Maybe.just("coupon-10") else Maybe.empty()

private fun findProductNamesFlowable(ids: List<Long>): Flowable<String> =
    Flowable.create({ emitter ->
        ids.forEach { id ->
            Thread.sleep(80)
            emitter.onNext("product-$id")
        }
        emitter.onComplete()
    }, BackpressureStrategy.BUFFER)

private fun completeCheckout(): Completable =
    Completable.fromAction {
        Thread.sleep(120)
    }

fun main() = runBlocking {
    val price = findPriceSingle(1L).await()
    log.info { "price: $price" }

    val coupon = findCouponMaybe(exists = false).awaitSingleOrNull()
    log.info { "coupon: $coupon" }

    val firstName = findProductNamesFlowable(listOf(1L, 2L, 3L)).firstOrError().await()
    log.info { "firstName: $firstName" }

    val allNames = findProductNamesFlowable(listOf(1L, 2L, 3L)).toList().await()
    log.info { "allNames: $allNames" }

    completeCheckout().await()
    log.info { "checkout completed" }
}
