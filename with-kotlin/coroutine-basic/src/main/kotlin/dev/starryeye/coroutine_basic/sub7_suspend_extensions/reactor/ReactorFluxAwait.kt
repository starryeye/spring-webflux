package dev.starryeye.coroutine_basic.sub7_suspend_extensions.reactor

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

/**
 * Reactor Flux 용 suspend 확장 함수
 *
 * 위치
 *      Flux 는 Reactor 타입이지만 Publisher<T> 이기도 하다.
 *      그래서 Flux 전용 await* 가 따로 있는 것이 아니라,
 *      reactive 쪽 awaitFirst / awaitFirstOrNull / awaitLast / awaitSingle 가 그대로 적용된다
 *      (etc/ReactivePublisherAwait.kt 참고).
 *      이 파일은 Flux 를 받아왔을 때 실무에서 자주 마주치는 3가지 패턴을 정리한다.
 *
 * 자주 쓰는 패턴
 *      [1] 첫 값 하나만 필요    -> flux.awaitFirst() / awaitFirstOrNull()
 *      [2] 끝까지 모은 List    -> flux.collectList().awaitSingle()       (Reactor operator 로 모음)
 *                                또는 flux.asFlow().toList()              (coroutine Flow 로 모음)
 *      [3] 한 건씩 흘려서 처리  -> flux.asFlow().collect { ... }
 *
 * [1] vs [2] vs [3] 의 선택
 *      [1] : 결과가 1개로 충분할 때.
 *      [2] : N 개를 한꺼번에 List 로 다루고 싶을 때.
 *            collectList() 는 Reactor 가 List 로 모아 단일 Mono 로 만들어주는 operator.
 *            asFlow().toList() 는 coroutine 쪽에서 모으는 방식. 결과는 같다.
 *            이후 처리를 Reactor 체인으로 잇고 싶다면 collectList(),
 *            Flow operator (map / filter / ...) 로 잇고 싶다면 asFlow().
 *      [3] : 한 건씩 흘려서 처리해야 할 때 (DB cursor / SSE / 무한 stream 등).
 *            awaitLast 처럼 "끝" 을 전제로 하는 함수와 달리 무한 스트림에도 안전하다.
 *
 * 스레드 실행 모델 (QnA Q5 의 Flux 버전)
 *      Mono 와 같은 그림이지만, 두 갈래로 나뉜다.
 *
 *      [a] awaitFirst() / collectList().awaitSingle() (단일 값으로 줄여서 await)
 *          Mono 의 awaitSingle 과 동일하다.
 *          emit 스레드 = subscribeOn / publishOn 으로 결정,
 *          resume 스레드 = 코루틴의 Dispatcher (runBlocking 이면 main, withContext(IO) 면 IO worker).
 *
 *      [b] asFlow().collect { ... }
 *          내부적으로 채널을 한 번 거친다.
 *          emit 스레드 = Reactor Scheduler 가 onNext 를 호출하는 스레드 (channel.send 가 거기서 일어남).
 *          collect 람다 스레드 = "collect 를 호출한 coroutine 의 context" 가 결정.
 *              -> runBlocking 이면 main, withContext(Dispatchers.IO) 면 IO worker.
 *          즉 publishOn(Scheduler) 는 람다 실행 스레드를 직접 바꾸지 못한다. 채널 경계 때문이다.
 *
 *      한 줄 요약: "다음 줄을 어디서 돌릴지" 는 결국 코루틴 Dispatcher 가 결정한다.
 *      Reactor Scheduler 는 emit 스레드까지만 책임진다.
 *
 * 취소 (cancellation)
 *      coroutine 이 cancel 되면 awaitFirst / asFlow().collect 모두 내부적으로 Subscription.cancel() 을 호출한다.
 *          덕분에 무한 Flux 도 안전하게 끊을 수 있다.
 *      sub6 의 FirstFinder 에서 손으로 했던 "받자마자 unsubscribe" 가 라이브러리 차원에서 자동화된 것이다.
 *
 * 참고
 *      - flux { suspend block; emit(...) } / kotlinx.coroutines.reactor.flux
 *        suspend 코드를 다시 Flux 로 노출하고 싶을 때 (예: Spring WebFlux Controller 호환).
 *      - Flow<T>.asFlux() : 반대 방향 (Flow -> Flux).
 */
private val log = KotlinLogging.logger {}

/**
 * 학습용 Flux. 실제 운영에선 R2DBC / WebClient.bodyToFlux / Sinks 등이 자리한다.
 */
private fun productPriceFlux(productIds: List<Long>): Flux<Long> =
    Flux.create { sink ->
        productIds.forEach { id ->
            Thread.sleep(80)
            sink.next(1000L + id)
        }
        sink.complete()
    }

/**
 * emit 스레드를 로그로 찍어주는 헬퍼.
 */
private fun describingFlux(tag: String, count: Int): Flux<String> =
    Flux.create { sink ->
        repeat(count) { i ->
            log.info { "[$tag] emit#$i thread: ${Thread.currentThread().name}" }
            sink.next("$tag-$i")
        }
        sink.complete()
    }

fun main() = runBlocking {
    val ids = listOf(1L, 2L, 3L)

    // [1] 첫 값 하나만 필요
    val firstPrice = productPriceFlux(ids).awaitFirst()
    log.info { "firstPrice: $firstPrice" }

    val firstOrNull = productPriceFlux(emptyList()).awaitFirstOrNull()
    log.info { "firstOrNull: $firstOrNull" }

    // [2-a] 끝까지 모은 List - Reactor operator 로
    val allByCollect: List<Long> = productPriceFlux(ids).collectList().awaitSingle()
    log.info { "allByCollect: $allByCollect" }

    // [2-b] 끝까지 모은 List - asFlow().toList() 로
    val allByFlow: List<Long> = productPriceFlux(ids).asFlow().toList()
    log.info { "allByFlow: $allByFlow" }

    // [3] 한 건씩 흘려서 처리
    productPriceFlux(ids).asFlow().collect { price ->
        log.info { "streamed: $price" }
    }

    // 스레드 실행 모델 확인 -------------------------------------------------

    // a) subscribeOn 없음 + runBlocking
    //    구독 시작 스레드 = main 이라 emit 도 main, collect 람다도 main.
    describingFlux("a-no-scheduler", 2).asFlow().collect {
        log.info { "[a] collect thread: ${Thread.currentThread().name}, item: $it" }
    }

    // b) subscribeOn(parallel) + runBlocking
    //    emit 은 parallel-N 로 옮겨졌다.
    //    그러나 collect 람다는 main.
    //        asFlow 가 채널을 한 번 거치고, runBlocking 의 Dispatcher 가 main 이라
    //        실제 람다 호출은 main 에서 일어난다.
    describingFlux("b-subscribeOn", 2)
        .subscribeOn(Schedulers.parallel())
        .asFlow().collect {
            log.info { "[b] collect thread: ${Thread.currentThread().name}, item: $it" }
        }

    // c) subscribeOn(parallel) + withContext(Dispatchers.IO)
    //    emit 은 parallel-N, collect 람다는 DefaultDispatcher-worker-N (Dispatchers.IO 풀).
    //    Mono 의 4-c 케이스와 의미가 같다. "다음 줄" 은 항상 코루틴 Dispatcher 가 결정한다.
    withContext(Dispatchers.IO) {
        describingFlux("c-subscribeOn-io", 2)
            .subscribeOn(Schedulers.parallel())
            .asFlow().collect {
                log.info { "[c] collect thread: ${Thread.currentThread().name}, item: $it" }
            }
    }
}
