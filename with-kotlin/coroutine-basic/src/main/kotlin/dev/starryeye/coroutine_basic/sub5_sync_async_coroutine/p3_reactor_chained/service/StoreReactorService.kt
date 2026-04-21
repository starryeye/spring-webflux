package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p3_reactor_chained.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Store
import reactor.core.publisher.Flux

class StoreReactorService {

    fun findStoresFlux(storeIds: List<Long>): Flux<Store> {
        return Flux.create { sink ->
            storeIds.distinct().forEach { id ->
                Thread.sleep(100)
                sink.next(Store(id = id, name = "매장 $id"))
            }
            sink.complete()
        }
    }
}
