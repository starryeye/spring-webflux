package dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Product
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable

/**
 * 상품 정보를 RxJava3 의 Flowable 로 반환한다.
 */
class ProductRxjava3Service {

    fun findAllProductsFlowable(ids: List<Long>): Flowable<Product> {
        return Flowable.create({ emitter ->
            ids.forEach {
                Thread.sleep(100)
                val p = Product(id = it, name = "상품 $it", price = 1000L + it)
                emitter.onNext(p)
            }
            emitter.onComplete()
        }, BackpressureStrategy.BUFFER)
    }
}
