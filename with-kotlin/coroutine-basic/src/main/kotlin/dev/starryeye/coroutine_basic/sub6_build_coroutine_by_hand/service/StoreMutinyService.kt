package dev.starryeye.coroutine_basic.sub6_build_coroutine_by_hand.service

import dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.common.Store
import io.smallrye.mutiny.Multi

/**
 * 스토어 정보를 Mutiny 의 Multi 로 반환한다.
 */
class StoreMutinyService {

    fun findStoresMulti(storeIds: List<Long>): Multi<Store> {
        return Multi.createFrom().emitter { emitter ->
            storeIds.distinct().map { id ->
                Store(id = id, name = "매장 $id")
            }.forEach { store ->
                Thread.sleep(100)
                emitter.emit(store)
            }
            emitter.complete()
        }
    }
}
