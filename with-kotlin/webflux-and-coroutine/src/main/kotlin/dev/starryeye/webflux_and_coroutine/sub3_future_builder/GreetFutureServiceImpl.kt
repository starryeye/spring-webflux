package dev.starryeye.webflux_and_coroutine.sub3_future_builder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/**
 * future { } 빌더 사용 예제.
 *
 * 핵심
 *      [GreetFutureService] 의 시그니처는 CompletableFuture<String> 를 반환해야 한다.
 *      그런데 호출하고 싶은 greeting() 은 suspend 함수다.
 *      future { } 빌더로 감싸서 결과를 CompletableFuture<String> 로 노출시키는 것이 표준 패턴이다.
 *
 *      future { } 빌더의 위치
 *          kotlinx.coroutines.future.future
 *          (artifact: kotlinx-coroutines-jdk8)
 *
 *      sub2 의 mono { } 와의 차이
 *          mono { }    : top-level 함수. 인자 없이 mono { ... } 형태로 바로 호출 가능.
 *                        내부적으로 GlobalScope 가 쓰인다.
 *          future { }  : CoroutineScope 의 확장 함수. 그래서 호출하려면 scope 가 있어야 한다.
 *                        예) CoroutineScope(Dispatchers.IO).future { ... }
 *
 *          이 차이 때문에 sub3 에서는 명시적으로 CoroutineScope 를 만들어 넘긴다.
 *
 * 동작 확인 - 아래 [main] 을 실행하면 다음과 비슷한 로그가 찍힌다.
 *
 *      53:28 [DefaultDispatcher-worker-1 @coroutine#1] - greet: hello
 *
 *      두 가지 관찰
 *          1. 우리는 Dispatchers.IO 를 넘겼는데 스레드 이름은 "DefaultDispatcher-worker-*" 다.
 *             Dispatchers.IO 는 내부적으로 Dispatchers.Default 와 같은 워커 풀을 공유하기 때문이다.
 *             (IO 와 CPU bound 사이에서 스레드를 양보/확장하는 정책만 다르다.)
 *          2. "@coroutine#1" 표식이 함께 찍힌다 = 정말 코루틴 위에서 실행됐다.
 *
 *      Spring 의 컨트롤러 케이스(sub1) 와 비교하면
 *          - sub1 은 Spring 이 내부에서 mono(Dispatchers.Unconfined) { ... } 를 부른다.
 *          - sub3 은 사용자가 직접 CoroutineScope(Dispatchers.IO).future { ... } 를 부른다.
 *      "누가 코루틴 빌더를 부르냐" 가 다를 뿐 본질은 같다.
 */
class GreetFutureServiceImpl : GreetFutureService {

    private suspend fun greeting(): String {
        delay(100)
        return "hello"
    }

    override fun findGreet(): CompletableFuture<String> {
        return CoroutineScope(Dispatchers.IO).future {
            greeting()
        }
    }
}

fun main() {
    val log = LoggerFactory.getLogger(GreetFutureServiceImpl::class.java)
    val greetFutureService = GreetFutureServiceImpl()
    greetFutureService.findGreet()
        .thenAccept { greet ->
            log.info("greet: {}", greet)
        }
    Thread.sleep(1000)
}
