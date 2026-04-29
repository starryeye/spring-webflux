package dev.starryeye.webflux_and_coroutine.sub2_mono_builder

import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import kotlin.coroutines.coroutineContext

/**
 * Reactor Context 가 mono { } 빌더 안의 suspend 함수까지 전달되는지 검증하는 예제.
 *
 * 흐름
 *      1. main 함수의 .contextWrite { it.put("who", "starryeye") } 로
 *         "who" -> "starryeye" 라는 항목을 Reactor Context 에 주입한다.
 *      2. findGreet() 의 mono { } 빌더가 그 Reactor Context 를 받아서
 *         코루틴 컨텍스트(ReactorContext 키 아래) 로 변환해 넣어준다.
 *      3. greeting() 안에서 coroutineContext[ReactorContext]?.context?.get<String>("who")
 *         로 다시 꺼낼 수 있다.
 *
 * 등장하는 ReactorContext 의 위치
 *      kotlinx.coroutines.reactor.ReactorContext
 *      (artifact: kotlinx-coroutines-reactor)
 *
 *      이 클래스는 CoroutineContext.Element 의 키이며, 그 안에 Reactor 의 Context 를 들고 있다.
 *      즉 "Reactor Context 를 코루틴 컨텍스트에 끼워 넣기 위한 어댑터" 라고 생각하면 된다.
 *
 * 동작 확인 - 아래 [main] 을 실행하면 다음과 비슷한 로그가 찍힌다.
 *
 *      45:18 [DefaultDispatcher-worker-2 @coroutine#1] - greet: hello, starryeye
 *
 *      "starryeye" 가 출력됐다는 것은 main 의 .contextWrite 으로 넣은 값이
 *      코루틴 안 suspend 함수까지 정상적으로 도달했다는 뜻.
 *
 *      만약 .contextWrite 을 빼면 ?: "world" 분기로 떨어져 "hello, world" 가 찍힌다.
 *
 * 누가 이걸 처리해주는가
 *      mono { } 빌더 안의 monoInternal 이 sink.currentContext() 에서 Reactor Context 를 꺼내
 *      코루틴 컨텍스트에 합쳐준다. 자세한 내부 동작은 같은 패키지의 [Internals] 참고.
 */
class GreetMonoServiceImpl2 : GreetMonoService {

    // 넣고 싶은 함수
    private suspend fun greeting(): String {
        delay(100)
        val who = coroutineContext[ReactorContext]
            ?.context
            ?.get<String>("who")
            ?: "world"
        return "hello, $who"
    }

    // suspend 함수를 내부에서 호출하고 싶은 변경 불가 시그니처 함수
    override fun findGreet(): Mono<String> {
        return mono {
            greeting()
        }
    }
}

fun main() {
    val log = LoggerFactory.getLogger(GreetMonoServiceImpl2::class.java)

    val greetMonoService = GreetMonoServiceImpl2()

    greetMonoService.findGreet()
        .contextWrite { it.put("who", "starryeye") }
        .subscribe { greet ->
            log.info("greet: {}", greet)
        }
    Thread.sleep(1000)
}
