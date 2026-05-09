package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub6_coroutine_exception_handler.sub2_coroutine_exception_handler

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * CoroutineExceptionHandler 예제 (3) - async 에는 handler 가 적용되지 않는다
 *
 * 시나리오
 *      async 가 만든 root scope 의 context 에 handler 를 함께 주입해본다.
 *
 *      val deferred = CoroutineScope(Dispatchers.IO + handler).async {
 *          throw IllegalStateException("exception in launch")
 *          10
 *      }
 *
 * 결과
 *      handler 는 무시되고, await() 호출 측의 try-catch 만 동작한다.
 *
 * 이유
 *      - async 의 exception 은 Deferred 안에 "보관" 된다.
 *      - 그 보관된 exception 은 await() 시점에 caller 로 다시 던져진다.
 *      - 즉 exception 의 책임이 "Deferred 의 소비자(=await 호출자)" 에게 있다는 모델이라
 *          handler 가 가로챌 여지가 없다
 *      - 따라서 async 트리에서는 handler 대신 await 를 try-catch 로 감싸 처리해야 한다.
 *
 *
 * 출력
 *      [main @coroutine#1] - exception caught in catch
 *      (note: "exception caught in handler" 는 안 찍힘)
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val handler = CoroutineExceptionHandler { _, e ->
            log.error { "exception caught in handler" }   // 안 찍힘
        }

        val deferred = CoroutineScope(context = Dispatchers.IO + handler).async {
            throw IllegalStateException("exception in launch")
            10
        }

        // handler 는 무시되고, 결국 try-catch 로만 잡힌다.
        try {
            deferred.await()
        } catch (e: Exception) {
            log.info { "exception caught in catch" }
        }
    }
}
