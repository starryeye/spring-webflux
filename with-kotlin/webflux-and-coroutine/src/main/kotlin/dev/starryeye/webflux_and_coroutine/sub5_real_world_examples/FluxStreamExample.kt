package dev.starryeye.webflux_and_coroutine.sub5_real_world_examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.flux
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

/**
 * sub2 의 짝꿍 - Flux 반환 시그니처가 강제되는 자리.
 *
 * mono { } 와 같은 패턴이고 빌더만 flux { } 로 바뀐다.
 * 람다 안에서 send(value) 를 여러 번 호출하면 onNext, 람다 종료 시 onComplete 가 된다.
 *
 * 자주 나오는 자리
 *      - Spring Data R2DBC repository 의 findAll / findByX 류 (Flux<T> 반환)
 *      - Server-Sent Events 핸들러 (produces = MediaType.TEXT_EVENT_STREAM_VALUE)
 *      - Reactor Kafka / Reactor RabbitMQ consumer
 *      - WebClient 의 응답 스트림 (다만 이건 받는 쪽)
 */
interface NotificationStream {
    fun streamFor(userId: Long): Flux<String>
}

class CoroutineBackedNotificationStream : NotificationStream {

    private suspend fun fetchNotification(userId: Long, index: Int): String {
        delay(50)
        return "notif-$userId-$index"
    }

    override fun streamFor(userId: Long): Flux<String> = flux {
        repeat(3) { i ->
            send(fetchNotification(userId, i))
        }
    }
}

fun main() {
    val log = LoggerFactory.getLogger(CoroutineBackedNotificationStream::class.java)
    val stream = CoroutineBackedNotificationStream()
    stream.streamFor(42L)
        .subscribe { notif -> log.info("received: {}", notif) }
    Thread.sleep(500)
}
