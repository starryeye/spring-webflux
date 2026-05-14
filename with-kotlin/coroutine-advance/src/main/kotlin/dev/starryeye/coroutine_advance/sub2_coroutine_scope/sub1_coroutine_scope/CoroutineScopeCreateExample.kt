package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub1_coroutine_scope

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

/**
 * CoroutineScope 생성 예제 (PDF 169p)
 *
 * 시나리오
 *      val cs = CoroutineScope(EmptyCoroutineContext)
 *
 *      log.info { "context: ${cs.coroutineContext}" }
 *      log.info { "class name: ${cs.javaClass.simpleName}" }
 *
 *
 * 관찰 포인트
 *      - EmptyCoroutineContext (말 그대로 텅 빈 context) 를 넘겼다.
 *      - 그런데 cs.coroutineContext 출력에 JobImpl{Active}@... 이 들어있다.
 *          -> CoroutineScope() 팩토리가 "context 에 Job 이 없네?" 하고 Job() 으로 새 JobImpl 을 만들어 채워 넣은 결과.
 *          -> 직전 파일 (CoroutineScopeFunction.kt) 에서 본 동작을 출력으로 확인하는 셈.
 *      - cs.javaClass.simpleName 은 ContextScope.
 *          -> 우리가 만든 scope 의 실체가 ContextScope 라는 단순 구현체임을 확인.
 *
 *      즉 "EmptyCoroutineContext 로 만들었으니 텅 빈 scope" 라는 표현은 잘못된 표현이고,
 *      실제로는 새로 만들어진 JobImpl 을 하나 가지고 있는 ContextScope 다.
 *
 *
 * 출력 예시
 *      [main] - context: JobImpl{Active}@57250572
 *      [main] - class name: ContextScope
 */
private val log = KotlinLogging.logger {}

fun main() {
    val cs = CoroutineScope(context = EmptyCoroutineContext)

    log.info { "context: ${cs.coroutineContext}" }
    log.info { "class name: ${cs.javaClass.simpleName}" }
}
