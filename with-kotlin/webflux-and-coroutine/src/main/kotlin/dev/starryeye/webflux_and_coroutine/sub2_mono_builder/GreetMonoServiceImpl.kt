package dev.starryeye.webflux_and_coroutine.sub2_mono_builder

import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

/**
 * mono { } 빌더 사용 예제.
 *
 * 핵심
 *      [GreetMonoService] 의 시그니처는 Mono<String> 를 반환해야 한다.
 *      그런데 호출하고 싶은 greeting() 은 suspend 함수다.
 *      mono { } 빌더로 감싸서 결과를 Mono<String> 로 노출시키는 것이 표준 패턴이다.
 *
 *      mono { } 빌더의 위치
 *          kotlinx.coroutines.reactor.MonoKt#mono
 *          (artifact: kotlinx-coroutines-reactor)
 *
 * 동작 확인 - 아래 [main] 을 실행하면 다음과 비슷한 로그가 찍힌다.
 *
 *      36:10 [DefaultDispatcher-worker-2 @coroutine#1] - greet: hello
 *
 *      두 가지 관찰
 *          1. 스레드 이름이 "DefaultDispatcher-worker-*" 다.
 *             mono { } 빌더에 별도 dispatcher 인자를 안 주면 기본 Dispatcher (Default) 가 쓰인다.
 *             (sub1 의 컨트롤러 케이스에서는 Dispatchers.Unconfined 였다.
 *              그쪽은 Spring 이 invokeSuspendingFunction 안에서 명시적으로 Unconfined 를 넘겼기 때문.)
 *          2. "@coroutine#1" 표식이 함께 찍힌다 = 정말 코루틴 위에서 실행됐다.
 *
 *      "그럼 Reactor 의 Context 는 이 코루틴까지 전달이 될까?" 의 답은 [GreetMonoServiceImpl2] 에서.
 */
class GreetMonoServiceImpl : GreetMonoService {

    private suspend fun greeting(): String {
        delay(100)
        return "hello"
    }

    override fun findGreet(): Mono<String> {
        return mono {
            greeting()
        }
    }
}

fun main() {
    val log = LoggerFactory.getLogger(GreetMonoServiceImpl::class.java)
    val greetMonoService = GreetMonoServiceImpl()
    greetMonoService.findGreet()
        .subscribe { greet ->
            log.info("greet: {}", greet)
        }
    Thread.sleep(1000)
}
