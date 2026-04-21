package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.DeliveryAddress
import reactor.core.publisher.Flux

class DeliveryAddressReactorService {

    fun findDeliveryAddressesFlux(ids: List<Long>): Flux<DeliveryAddress> {
        return Flux.create { sink ->
            ids.forEach { id ->
                Thread.sleep(100)
                sink.next(
                    DeliveryAddress(
                        id = id,
                        roadNameAddress = "도로명 주소 $id",
                        detailAddress = "상세 주소 $id",
                    )
                )
            }
            sink.complete()
        }
    }
}
