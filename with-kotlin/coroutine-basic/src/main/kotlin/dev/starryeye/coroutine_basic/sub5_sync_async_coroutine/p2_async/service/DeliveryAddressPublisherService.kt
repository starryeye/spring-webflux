package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p2_async.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.DeliveryAddress
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux

/**
 * 배송지를 reactive-streams 의 Publisher 로 반환한다.
 *      구현 편의를 위해 Flux 를 사용한다.
 *      Flux 는 reactive-streams 의 Publisher 를 구현하고 있으므로 반환 가능하다.
 */
class DeliveryAddressPublisherService {

    fun findDeliveryAddressesPublisher(ids: List<Long>): Publisher<DeliveryAddress> {
        return Flux.create { sink ->
            ids.map { id ->
                DeliveryAddress(
                    id = id,
                    roadNameAddress = "도로명 주소 $id",
                    detailAddress = "상세 주소 $id",
                )
            }.forEach {
                Thread.sleep(100)
                sink.next(it)
            }
            sink.complete()
        }
    }
}
