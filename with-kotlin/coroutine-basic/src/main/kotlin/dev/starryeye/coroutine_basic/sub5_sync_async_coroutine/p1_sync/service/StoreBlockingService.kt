package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p1_sync.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Store

class StoreBlockingService {

    fun findStoresByIds(storeIds: List<Long>): List<Store> {
        return storeIds.distinct().map { id ->
            Thread.sleep(100)
            Store(id = id, name = "매장 $id")
        }
    }
}
