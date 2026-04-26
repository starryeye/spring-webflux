package dev.starryeye.coroutine_basic.sub7_suspend_extensions.reactor

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Reactor Mono 용 suspend 확장 함수
 *
 * sub5.p4 의 마지막 줄
 *      orderService.createOrderMono(...).awaitSingle()
 *
 * package
 *      kotlinx.coroutines.reactor
 *
 * 자주 쓰는 대상
 *      Mono<T>.awaitSingle()        // 정확히 1개. 비어 있으면 NoSuchElementException
 *      Mono<T>.awaitSingleOrNull()  // 0..1 개. 비어 있으면 null
 *
 * 포인트
 *      Mono 는 0..1 개를 내보내는 타입이다.
 *      값이 반드시 있어야 하면 awaitSingle(), 비어 있어도 되면 awaitSingleOrNull() 을 쓴다.
 *      Flux 는 Mono 가 아니므로 이 두 함수를 그대로 쓸 수 없다 -> reactor/ReactorFluxAwait.kt 참고.
 *
 * 실패 처리
 *      Mono.error(e) 를 await 하면 예외가 그대로 호출 지점에서 던져진다.
 *      Reactor 의 onErrorResume / onErrorReturn 같은 operator 로 잡을 수도 있고,
 *          coroutine 쪽 try-catch / runCatching 으로 잡아도 된다.
 *      "Mono 체인 중간에서 분기" 가 필요하면 operator,
 *          "값을 받은 뒤 분기" 면 try-catch 가 자연스럽다.
 *
 * 스레드 실행 모델 (QnA Q5 의 Mono 버전)
 *      "await 다음 줄" 을 누가 실행하는지는 두 단계로 결정된다.
 *      1) Mono 가 emit 하는 스레드 - subscribeOn / publishOn 으로 결정된다.
 *         지정이 없으면 구독을 시작한 스레드 (예: runBlocking 의 main).
 *      2) 다음 줄 실행은 코루틴의 Dispatcher 가 한 번 더 개입한다.
 *         runBlocking 내부면 main, withContext(Dispatchers.IO) 면 IO worker.
 *         Dispatcher 가 사실상 없는 경우 (Dispatchers.Unconfined / sub6 의 수작업 버전) 만
 *             emit 스레드가 그대로 다음 줄까지 실행한다.
 *
 *      비유하면 1) = Reactor Scheduler, 2) = Reactor 의 publishOn(Scheduler) 가 항상 한 개 더 깔린 셈이다.
 *      아래 main 의 case 4-a / 4-b / 4-c 의 로그 thread 이름으로 직접 확인해 본다.
 *
 * 참고
 *      - mono { suspend block } / kotlinx.coroutines.reactor.mono
 *        suspend 함수를 다시 Mono 로 노출하고 싶을 때 (예: Spring WebFlux Controller 호환).
 *      - Mono.fromFuture(future) / future.asMono() 등 양방향 브릿지가 별도로 존재한다.
 */
private val log = KotlinLogging.logger {}

private fun createOrderMono(orderId: Long): Mono<String> =
    Mono.fromSupplier {
        Thread.sleep(150)
        "order-$orderId"
    }

private fun findOptionalOrderMono(found: Boolean): Mono<String> =
    if (found) Mono.just("order-1") else Mono.empty()

private fun failingMono(): Mono<String> =
    Mono.error(IllegalStateException("payment failed"))

/**
 * 어떤 스레드에서 emit 되는지 확인하기 위한 헬퍼.
 * Mono.fromCallable 의 supplier 는 "구독을 시작한 스레드" (subscribeOn 이 있으면 그 Scheduler) 위에서 실행된다.
 */
private fun describingMono(tag: String): Mono<String> =
    Mono.fromCallable {
        log.info { "[$tag] emit thread: ${Thread.currentThread().name}" }
        "value-$tag"
    }

fun main() = runBlocking {
    // 1) 기본
    val order = createOrderMono(1L).awaitSingle()
    log.info { "order: $order" }

    // 2) 비어있을 수 있는 Mono - awaitSingle 은 예외, awaitSingleOrNull 은 null
    val optionalOrder = findOptionalOrderMono(found = false).awaitSingleOrNull()
    log.info { "optionalOrder: $optionalOrder" }

    // 3) Mono.error 는 await 지점에서 예외로 보인다 -> coroutine 쪽 try-catch / runCatching 으로 잡으면 된다
    runCatching { failingMono().awaitSingle() }
        .onFailure { log.info { "caught: ${it::class.simpleName} - ${it.message}" } }

    // 4) 스레드 실행 모델 확인 -------------------------------------------------

    // 4-a) subscribeOn 없음 + runBlocking
    //      구독 시작 스레드 = main, 그래서 emit 도 main.
    //      runBlocking 의 Dispatcher 도 main 이라 resume 도 main.
    val a = describingMono("a-no-scheduler").awaitSingle()
    log.info { "[a] resume thread: ${Thread.currentThread().name}, value: $a" }

    // 4-b) subscribeOn(boundedElastic) + runBlocking
    //      emit 은 boundedElastic-N 로 옮겨졌다.
    //      그러나 resume 은 main.
    //          runBlocking 자체가 "main 스레드를 쓰는 Dispatcher" 라서,
    //          boundedElastic-N 가 cont.resumeWith 를 호출하면 다시 main 으로 dispatch 된다.
    val b = describingMono("b-bounded")
        .subscribeOn(Schedulers.boundedElastic())
        .awaitSingle()
    log.info { "[b] resume thread: ${Thread.currentThread().name}, value: $b" }

    // 4-c) subscribeOn(boundedElastic) + withContext(Dispatchers.IO)
    //      emit 은 boundedElastic-N, resume 은 DefaultDispatcher-worker-N (Dispatchers.IO 풀).
    //      "Reactor Scheduler 와 coroutine Dispatcher 가 서로 다른 풀" 인 일반적인 운영 케이스.
    withContext(Dispatchers.IO) {
        val c = describingMono("c-bounded-io")
            .subscribeOn(Schedulers.boundedElastic())
            .awaitSingle()
        log.info { "[c] resume thread: ${Thread.currentThread().name}, value: $c" }
    }
}
