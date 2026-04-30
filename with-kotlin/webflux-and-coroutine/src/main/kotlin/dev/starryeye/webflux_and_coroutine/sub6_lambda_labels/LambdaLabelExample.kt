package dev.starryeye.webflux_and_coroutine.sub6_lambda_labels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

/**
 * 라벨 사용 동작 예제.
 *
 * 세 가지 케이스를 한 화면에 보여준다.
 *      1. mono { } 람다에서 return@mono 로 조기 종료
 *      2. launch { } 람다에서 return@launch 로 조기 종료
 *      3. 코루틴과 무관한 forEach 에서 return@forEach (continue 효과)
 *
 * [main] 실행 시 출력 예
 *      cached hit, returning 'cached-result'
 *      mono result: cached-result
 *      launch: stopping early at i=2
 *      forEach: 1
 *      forEach: 3
 */
private val log = LoggerFactory.getLogger("LambdaLabelExample")

private fun findGreet(cached: String?): Mono<String> = mono {
    if (cached != null) {
        log.info("cached hit, returning '{}'", cached)
        return@mono cached     // mono 람다에서만 빠져나가며 cached 를 Mono 결과로 노출
    }
    delay(50)
    "loaded-from-db"
}

fun main() {
    // 1. mono 라벨
    findGreet(cached = "cached-result")
        .subscribe { result -> log.info("mono result: {}", result) }

    // 2. launch 라벨
    val scope = CoroutineScope(Dispatchers.Default)
    scope.launch {
        repeat(5) { i ->
            if (i == 2) {
                log.info("launch: stopping early at i={}", i)
                return@launch
            }
            // i == 0, 1 만 실행되고 i == 2 에서 launch 람다가 끝난다
        }
    }

    // 3. 일반 forEach 라벨 (continue 효과)
    runBlocking {
        listOf(1, 2, 3).forEach {
            if (it == 2) return@forEach     // 다음 요소로 넘어간다
            log.info("forEach: {}", it)
        }
    }

    Thread.sleep(300)
}
