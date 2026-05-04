package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub1_coroutine_name

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * CoroutineContext 의 종류 (1) - CoroutineName
 *
 * CoroutineName 이란
 *      - "이 coroutine 에 사람이 읽기 좋은 이름을 붙이기 위한" CoroutineContext 의 Element 다.
 *      - 실행 동작에 영향을 주지 않는, 순수 디버깅 / 로깅용 element.
 *      - 안 붙이면 기본값 "coroutine" 으로 표시되고, 붙이면 그 이름이 그대로 보인다.
 *
 * 정의 (kotlinx.coroutines.CoroutineName)
 *      public data class CoroutineName(
 *          val name: String
 *      ) : AbstractCoroutineContextElement(CoroutineName) {
 *
 *          public companion object Key : CoroutineContext.Key<CoroutineName>
 *
 *          override fun toString(): String = "CoroutineName($name)"
 *      }
 *
 *      읽는 포인트
 *          - data class -> equals/hashCode/toString/copy 자동 제공
 *          - AbstractCoroutineContextElement(CoroutineName) 를 상속 -> Element 구현체
 *              생성자에 자기 자신의 Key (companion object) 를 넘겨주는 형태이다.
 *              즉 "이 element 의 Key 가 무엇인지" 를 명시적으로 알려준다.
 *              (sub1_structure/sub4_key_and_element 에서 다룬 패턴 그대로)
 *          - companion object Key : CoroutineContext.Key<CoroutineName>
 *              -> coroutineContext[CoroutineName] 으로 꺼낼 때 이 Key 가 사용된다.
 *
 * VM options 에 "-Dkotlinx.coroutines.debug" 를 넣고 실행해야 보인다.
 *
 *
 * 이 예제가 보여주는 것
 *      - runBlocking 에도 CoroutineContext 인자를 넘길 수 있다는 점
 *          -> runBlocking(CoroutineName("runBlocking"))
 *          -> 처음부터 이름이 붙은 채로 시작한다.
 *      - withContext 로 진입하면 CoroutineName 만 갈아끼울 수 있다
 *          -> withContext(CoroutineName("withContext"))
 *          -> 같은 Key 를 plus 로 override 하므로 이름만 바뀐다.
 *
 * 출력 (-Dkotlinx.coroutines.debug 켰을 때)
 *      [main @runBlocking#1] - name in runBlocking: CoroutineName(runBlocking)
 *      [main @withContext#1] - name in withContext: CoroutineName(withContext)
 */
private val log = KotlinLogging.logger {}

fun main() {
    // runBlocking 에 CoroutineName 을 직접 주입 -> 시작부터 이름을 붙여 둔다.
    runBlocking(context = CoroutineName(name = "runBlocking")) {

        // coroutineContext[CoroutineName] 으로 현재 element 를 꺼내 본다.
        //      [CoroutineName] 의 [ ] 는 CoroutineContext.get(Key) 의 인덱스 문법
        //      -> CoroutineName 의 companion object Key 가 그대로 사용된다.
        log.info { "name in runBlocking: ${this.coroutineContext[CoroutineName]}" }

        // withContext 로 같은 Key (CoroutineName) 를 override
        //      -> plus(+) 규칙상 오른쪽 인자가 이기므로 안쪽에서는 "withContext" 로 보인다.
        withContext(CoroutineName(name = "withContext")) {
            log.info { "name in withContext: ${this.coroutineContext[CoroutineName]}" }
        }
    }
}
