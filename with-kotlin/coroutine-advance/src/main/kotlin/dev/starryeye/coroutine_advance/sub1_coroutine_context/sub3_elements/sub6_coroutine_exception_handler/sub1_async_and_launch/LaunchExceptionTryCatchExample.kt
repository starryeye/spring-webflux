package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub6_coroutine_exception_handler.sub1_async_and_launch

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Error handling (3) - launch 전체를 try-catch 로 감싸도 안 잡힌다
 *
 * 시도
 *      앞 예제(LaunchExceptionExample) 에서 launch 가 던진 exception 이 join() 으로 안 잡혔으니
 *      "그럼 launch 람다 안쪽에서 try-catch 로 감싸면 잡히겠지?" 하고 시도해본다.
 *
 *          launch {
 *              try {
 *                  launch {
 *                      launch { throw ... }
 *                  }
 *              } catch (e: Exception) {
 *                  ...   // 절대 찍히지 않음
 *              }
 *          }
 *
 * 결과
 *      여전히 잡히지 않는다. 그대로 stderr 에 raw exception 이 출력됨.
 *
 * 이유
 *      - launch 는 "함수 호출처럼 exception 이 위로 throw 되는" 구조가 아니다.
 *      - launch 는 호출 즉시 자식 Job 을 만들고 바로 리턴한다.
 *          람다 본문(throw 가 일어나는 곳) 은 "나중에" 다른 스레드에서 실행됨.
 *          -> 그 시점엔 이미 try 블록의 끝을 지나가 버린 상태이므로 catch 가 걸릴 리 없다.
 *      - 그 대신 자식 Job 의 fail 은 부모 Job 으로 cancellation 이 전파되며 함께 exception 이 전달된다.
 *          -> 일반 try-catch 의 "코드 흐름" 으로는 잡을 수 없는 경로다.
 *
 *      정리: "함수처럼 exception 이 전파되는 구조가 아니다" -> 일반 try-catch 로는 launch 의 exception 을 handle 불가.
 *
 *
 * 그래서 어떻게 잡나
 *      - CoroutineExceptionHandler 라는 CoroutineContext.Element 를 사용해야 한다 (다음 sub2 에서 다룸)
 *      - 또는 supervisorScope / SupervisorJob 으로 cancellation 전파 구조 자체를 바꿀 수도 있다 (별개 주제).
 *
 *
 * 출력
 *      Exception in thread "DefaultDispatcher-worker-2 @coroutine#4" java.lang.IllegalStateException: exception in launch
 *          at ...
 *
 *      (note: "not caught maybe" 는 절대 찍히지 않는다)
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        val job = CoroutineScope(context = Dispatchers.IO).launch {
            try {
                // 이 launch 는 "자식 coroutine 을 등록만 하고" 즉시 리턴한다.
                //      throw 는 아래 깊은 launch 의 본문이 비동기로 실행될 때 발생 -> 이 try 블록을 이미 빠져나간 뒤다.
                launch {
                    launch {
                        throw IllegalStateException("exception in launch")
                    }
                }
            } catch (e: Exception) {
                log.error { "not caught maybe" }   // 안 찍힘
            }
        }

        job.join()
    }
}
