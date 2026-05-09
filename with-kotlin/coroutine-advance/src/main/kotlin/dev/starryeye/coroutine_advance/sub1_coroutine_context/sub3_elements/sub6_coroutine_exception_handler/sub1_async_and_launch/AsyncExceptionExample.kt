package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub6_coroutine_exception_handler.sub1_async_and_launch

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * Error handling (1) - async 의 exception handling
 *
 * 핵심
 *      async 가 만든 Deferred 는 결과(또는 예외) 를 자기 안에 보관해뒀다가
 *      await() 시점에 caller 에게 노출한다.
 *      따라서 await() 를 try-catch 로 감싸면 일반 함수처럼 exception 을 잡을 수 있다.
 *
 *      "async 는 exception 이 발생한 경우 user 에게 그 exception 을 노출" (await 시점에)
 *
 *
 * 출력 예시
 *      [main @coroutine#1] - exception caught
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        // 별도 scope (CoroutineScope(Dispatchers.IO)) 의 async
        //      runBlocking 의 자식이 아니라서, 안에서 던진 exception 이 곧장 부모 runBlocking 으로 전파되지 않는다.
        val deferred = CoroutineScope(context = Dispatchers.IO).async {
            throw IllegalStateException("exception in launch")
            10
        }

        // await() 가 exception 을 다시 던져준다 -> 보통 함수 호출처럼 try-catch 로 잡힌다.
        try {
            deferred.await()
        } catch (e: Exception) {
            log.info { "exception caught" }
        }
    }
}
