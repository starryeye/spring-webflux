package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.DeliveryAddress

class DeliveryAddressBlockingService {

    fun findDeliveryAddresses(ids: List<Long>): List<DeliveryAddress> {
        return ids.map { id ->
            Thread.sleep(100)
            DeliveryAddress(
                id = id,
                roadNameAddress = "도로명 주소 $id",
                detailAddress = "상세 주소 $id",
            )
        }
    }
}
