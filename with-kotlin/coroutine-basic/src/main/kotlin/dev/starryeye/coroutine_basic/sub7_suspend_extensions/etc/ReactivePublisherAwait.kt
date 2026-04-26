package dev.starryeye.coroutine_basic.sub7_suspend_extensions.etc

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux

/**
 * reactive-streams Publisher 용 suspend 확장 함수
 *
 * 위치 (etc 에 있는 이유)
 *      Publisher 는 reactive-streams 스펙의 인터페이스다. Reactor 전용이 아니다.
 *      Reactor 의 Mono / Flux 도, RxJava 의 Flowable 도 Publisher 를 구현한다.
 *      그래서 "외부 라이브러리들이 공통으로 따르는 추상" 으로 보고 etc 에 둔다.
 *      Reactor 전용 패턴 (Mono/Flux) 은 reactor/ 패키지 참고.
 *
 * sub5.p4 의 네 번째 줄
 *      deliveryAddressService.findDeliveryAddressesPublisher(daIds).awaitFirst()
 *
 * package
 *      kotlinx.coroutines.reactive
 *
 * 자주 쓰는 대상
 *      awaitFirst()              // 첫 값. 비어있으면 NoSuchElementException
 *      awaitFirstOrNull()        // 첫 값 또는 null
 *      awaitFirstOrDefault(d)    // 첫 값 또는 default
 *      awaitFirstOrElse { ... }  // 첫 값 또는 lazy default
 *      awaitLast()               // 끝까지 소비한 뒤 마지막 값
 *      awaitSingle()             // 정확히 1개여야 함 (0개나 2개+ 면 예외)
 *
 * 포인트
 *      Publisher 는 "몇 개가 올지" 가 정해지지 않은 추상 타입이다.
 *      그래서 계약(0개 허용 / 1개 필수 / 마지막만 필요 / ...) 에 맞춰
 *          first / firstOrNull / firstOrDefault / last / single 중에 골라야 한다.
 *      sub5.p4 는 배송지 여러 개 중 첫 번째만 필요했으므로 awaitFirst() 를 썼다.
 *
 * 주의
 *      awaitLast 는 끝까지 소비하므로 무한 스트림에는 쓰면 안된다.
 *      Reactor 의 Flux 도 Publisher 이므로 위 확장 함수들이 그대로 적용된다.
 *      (Flux 만의 별도 await 함수는 없다. Mono 만 awaitSingle / awaitSingleOrNull 을 추가로 가진다.)
 *
 * 참고
 *      - Publisher<T>.asFlow()    : Publisher -> Flow 변환
 *      - Flow<T>.asPublisher()    : 반대 방향 (suspend -> Publisher)
 */
private val log = KotlinLogging.logger {}

private fun findAddressPublisher(ids: List<Long>): Publisher<String> =
    Flux.create { sink ->
        ids.forEach { id ->
            Thread.sleep(80)
            sink.next("address-$id")
        }
        sink.complete()
    }

private fun findEmptyPublisher(): Publisher<String> = Flux.empty()

private fun findTokenPublisher(): Publisher<String> = Flux.just("token-1")

fun main() = runBlocking {
    // 1) 첫 값
    val firstAddress = findAddressPublisher(listOf(1L, 2L, 3L)).awaitFirst()
    log.info { "firstAddress: $firstAddress" }

    // 2) 첫 값 또는 null - "없을 수도 있다" 를 타입(Nullable) 으로 표현하고 싶을 때
    val firstOrNull: String? = findEmptyPublisher().awaitFirstOrNull()
    log.info { "firstOrNull: $firstOrNull" }

    // 3) 첫 값 또는 default
    val firstOrDefault = findEmptyPublisher().awaitFirstOrDefault("address-default")
    log.info { "firstOrDefault: $firstOrDefault" }

    // 4) 첫 값 또는 lazy default - default 계산이 비싸서 빈 경우에만 만들고 싶을 때
    val firstOrElse = findEmptyPublisher().awaitFirstOrElse { "address-${System.currentTimeMillis()}" }
    log.info { "firstOrElse: $firstOrElse" }

    // 5) 끝까지 소비 후 마지막 값 - 무한 스트림에는 쓰면 안된다
    val lastAddress = findAddressPublisher(listOf(1L, 2L, 3L)).awaitLast()
    log.info { "lastAddress: $lastAddress" }

    // 6) 정확히 1개여야 할 때
    val token = findTokenPublisher().awaitSingle()
    log.info { "token: $token" }
}
