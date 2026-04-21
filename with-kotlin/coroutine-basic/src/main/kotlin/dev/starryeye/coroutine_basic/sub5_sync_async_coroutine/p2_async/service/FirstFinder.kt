package dev.starryeye.coroutine_basic.sub5_sync_async_coroutine.p2_async.service

import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Publisher 에서 첫 번째로 emit 되는 값 하나만 받아서 onFirst 로 넘기는 Subscriber.
 *      Publisher 는 reactive-streams 의 기본 인터페이스로, 고수준 연산자(first, take 등) 가 없다.
 *      따라서 별도의 Subscriber 구현이 필요하다.
 */
class FirstFinder<T : Any>(
    private val onFirst: (T) -> Unit,
) : Subscriber<T> {

    private var found = false
    private lateinit var subscription: Subscription

    override fun onSubscribe(s: Subscription) {
        this.subscription = s
        s.request(1)
    }

    override fun onNext(t: T) {
        if (!found) {
            found = true
            onFirst(t)
            subscription.cancel()
        }
    }

    override fun onError(t: Throwable) {
        // 학습 목적: 에러 처리는 생략
    }

    override fun onComplete() {
        // no-op
    }
}
