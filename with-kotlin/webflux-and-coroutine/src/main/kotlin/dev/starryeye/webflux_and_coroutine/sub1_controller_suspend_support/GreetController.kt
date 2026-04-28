package dev.starryeye.webflux_and_coroutine.sub1_controller_suspend_support

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.coroutines.coroutineContext

/**
 * suspend 컨트롤러 예제
 *      Webflux 를 사용할 때, 코틀린 코루틴이 지원된다. (controller 에서 suspend 함수를 사용 가능)
 *
 * 로그 출력
 *      [reactor-http-nio-2] - context: [Context1{reactor.onDiscard.local=...},
 *                                        MonoCoroutine{Active}@xxxxxxxx,
 *                                        Dispatchers.Unconfined]
 *      [reactor-http-nio-2] - thread:  reactor-http-nio-2
 *
 *      이 출력이 말해주는 것
 *          1. coroutineContext 안에
 *              Reactor Context(Context1),
 *              MonoCoroutine,
 *              Dispatchers.Unconfined
 *              가 들어있다.
 *              즉 Spring 이 핸들러 호출을 코루틴으로 감싸 실행하고 있다는 뜻.
 *
 *          2. Dispatchers.Unconfined 이므로 별도의 스레드 풀로 점프하지 않고
 *              호출 스레드인 reactor-http-nio-2 위에서 그대로 코루틴이 진행된다.
 *
 *      "왜 별도의 코루틴 빌더 없이 컨트롤러에서 바로 suspend 함수를 부를 수 있는가" 의
 *          Spring 내부 동작은 같은 패키지의 [Overview] 참고.
 */
@RestController
@RequestMapping("/greet")
class GreetController {

    private val log = LoggerFactory.getLogger(GreetController::class.java)

    private suspend fun greeting(): String {
        return "hello"
    }

    @GetMapping
    suspend fun greet(): String {
        log.info("context: {}", coroutineContext)
        log.info("thread:  {}", Thread.currentThread().name)
        return greeting()
    }
}
