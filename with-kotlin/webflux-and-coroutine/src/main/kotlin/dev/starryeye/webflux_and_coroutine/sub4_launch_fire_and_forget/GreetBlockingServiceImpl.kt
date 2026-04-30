package dev.starryeye.webflux_and_coroutine.sub4_launch_fire_and_forget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * launch 빌더 사용 예제 (fire-and-forget).
 *
 * 핵심
 *      [GreetBlockingService] 의 시그니처는 Unit (반환값 없음) 이다.
 *      그래서 mono { } / future { } 처럼 결과를 흘려보낼 곳이 없다.
 *      그냥 코루틴 하나를 띄우고 본문을 돌리기만 하면 되는데, 그게 launch { } 빌더의 역할이다.
 *
 *      launch 빌더의 위치
 *          kotlinx.coroutines.Builders#launch (정확히는 BuildersKt.launch)
 *          (artifact: kotlinx-coroutines-core)
 *
 *          public fun CoroutineScope.launch(
 *              context: CoroutineContext = EmptyCoroutineContext,
 *              start: CoroutineStart = CoroutineStart.DEFAULT,
 *              block: suspend CoroutineScope.() -> Unit
 *          ): Job
 *
 *          - 람다는 suspend CoroutineScope 의 확장이라 안에서 suspend 함수를 그대로 호출 가능.
 *          - 반환은 Job. 값이 아니라 "그 코루틴의 수명 핸들" 이다.
 *
 * 동작 확인 - 아래 [main] 을 실행하면 다음과 비슷한 로그가 찍힌다.
 *
 *      02:44 [DefaultDispatcher-worker-1 @coroutine#1] - hello
 *
 *      ([Overview] section 0 의 Dispatchers.IO 설명 참고 - 스레드 이름이 DefaultDispatcher-* 인 이유)
 *
 * 주의
 *      이 패턴은 PDF 강의자료가 명시적으로 "권장하지는 않지만" 이라고 단 케이스다.
 *      쓰기 전에 [Overview] section 3 의 주의사항을 반드시 읽고 본인 상황이 거기에 맞는지 점검할 것.
 */
class GreetBlockingServiceImpl : GreetBlockingService {

    private val log = LoggerFactory.getLogger(GreetBlockingServiceImpl::class.java)

    private suspend fun greeting(): String {
        delay(100)
        return "hello"
    }

    override fun findGreet() {
        CoroutineScope(Dispatchers.IO).launch {
            log.info(greeting())
        }
    }
}

fun main() {
    val greetBlockingService = GreetBlockingServiceImpl()
    greetBlockingService.findGreet()
    Thread.sleep(1000)
}
