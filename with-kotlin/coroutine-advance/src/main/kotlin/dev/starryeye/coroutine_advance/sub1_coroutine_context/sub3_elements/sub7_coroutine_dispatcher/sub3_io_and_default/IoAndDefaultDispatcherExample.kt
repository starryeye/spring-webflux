package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub3_io_and_default

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.ContinuationInterceptor

/**
 * Default / IO dispatcher 사용 예제
 *
 * 시나리오
 *      runBlocking {
 *          [현재 dispatcher 출력]
 *          withContext(Default) { [현재 dispatcher 출력] }
 *          withContext(IO)      { [현재 dispatcher 출력] }
 *          CoroutineScope(CoroutineName("cs")).launch { [현재 dispatcher 출력] }
 *      }
 *
 *
 * 포인트
 *      1) runBlocking 의 dispatcher 는 BlockingEventLoop 다.
 *          - 호출 스레드(main) 를 그대로 점유하고 그 위에서 message queue 처럼 동작.
 *      2) withContext(Default), withContext(IO) 둘 다 DefaultDispatcher-worker-N 에서 실행됨.
 *          - 같은 worker pool 을 공유하기 때문 (io_default_dispatcher_shared 참고).
 *          - dispatcher 자체는 각각 Dispatchers.Default / Dispatchers.IO 로 다르게 보인다.
 *      3) CoroutineScope() 팩토리에 dispatcher 를 안 주면 -> Default 로 실행된다.
 *          - 본 예제는 CoroutineName("cs") 만 줬으므로 Default fallback.
 *
 *
 * 출력 예시
 *      [main @coroutine#1]                       - thread: main @coroutine#1
 *      [main @coroutine#1]                       - dispatcher: BlockingEventLoop@...
 *
 *      [DefaultDispatcher-worker-2 @coroutine#1] - thread: DefaultDispatcher-worker-2 @coroutine#1
 *      [DefaultDispatcher-worker-2 @coroutine#1] - dispatcher: Dispatchers.Default
 *
 *      [DefaultDispatcher-worker-2 @coroutine#1] - thread: DefaultDispatcher-worker-2 @coroutine#1
 *      [DefaultDispatcher-worker-2 @coroutine#1] - dispatcher: Dispatchers.IO
 *
 *      [DefaultDispatcher-worker-2 @cs#2]        - thread: DefaultDispatcher-worker-2 @cs#2
 *      [DefaultDispatcher-worker-2 @cs#2]        - dispatcher: Dispatchers.Default
 */
private val log = KotlinLogging.logger {}

// CoroutineScope.dispatcher() - 현재 context 의 CoroutineDispatcher 를 꺼내는 헬퍼.
//      ContinuationInterceptor Key 로 꺼낸 뒤 CoroutineDispatcher 로 캐스팅한다.
//      (CoroutineDispatcher.Key 가 polymorphic key 라 직접 쓰면 @ExperimentalStdlibApi opt-in 이 필요해서
//       이렇게 ContinuationInterceptor 슬롯에서 꺼내는 게 깔끔하다)
private fun CoroutineScope.dispatcher(): CoroutineDispatcher? =
    this.coroutineContext[ContinuationInterceptor] as? CoroutineDispatcher

fun main() {
    runBlocking {
        // (1) runBlocking - BlockingEventLoop dispatcher
        log.info { "thread: ${Thread.currentThread().name}" }
        log.info { "dispatcher: ${this.dispatcher()}" }

        // (2) withContext(Default) - Dispatchers.Default
        withContext(context = Dispatchers.Default) {
            log.info { "thread: ${Thread.currentThread().name}" }
            log.info { "dispatcher: ${this.dispatcher()}" }
        }

        // (3) withContext(IO) - Dispatchers.IO
        withContext(context = Dispatchers.IO) {
            log.info { "thread: ${Thread.currentThread().name}" }
            log.info { "dispatcher: ${this.dispatcher()}" }
        }

        // (4) CoroutineScope() 에 Dispatcher 인자가 없으면 Default 로 실행됨.
        //      CoroutineName 만 주입한 scope.
        CoroutineScope(context = CoroutineName(name = "cs")).launch {
            log.info { "thread: ${Thread.currentThread().name}" }
            log.info { "dispatcher: ${this.dispatcher()}" }
        }.join()
    }
}
