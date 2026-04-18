package dev.starryeye.coroutine_basic.sub3_continuation_passing_style

import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Q. Continuation 과 callback 의 차이는 무엇인가요?
 *      함수를 인자로 넘기고 나중에 호출한다는 점에서는 비슷해 보인다.
 *      하지만 "언제, 몇 번 호출되는가" 와 "어떤 의미를 가지는가" 가 다르다.
 *
 * Callback
 *      "추가로 무엇을 해야 하는지" 를 정의한 함수
 *      특정 이벤트가 발생했을 때 호출된다.
 *      따라서 어디에서나 여러 번 호출될 수 있다.
 *
 * Continuation
 *      "다음에 무엇을 해야 하는지" 를 정의한 함수
 *      모든 결과를 계산하고 다음으로 넘어가는 상황에서 호출된다.
 *      따라서 마지막에서 딱 한 번만 호출된다.
 *      로직의 제어를 넘긴다 라고도 볼 수 있다.
 *
 * 예제
 *      버튼이 5번 눌렸다고 가정하고
 *          callback 은 버튼이 눌릴 때마다 (이벤트 발생 시마다) 5회 호출된다.
 *          continuation 은 모든 클릭이 끝난 뒤 최종 카운트와 함께 1회만 호출된다.
 */
private val log = KotlinLogging.logger {}

object CallbackExample {

    fun handleButtonClicked(
        callback: () -> Unit, // 버튼이 눌릴 때마다 호출 -> 여러 번 호출됨
        continuation: (count: Int) -> Unit, // 모든 처리가 끝난 후 단 한 번 호출됨
    ) {
        var count = 0

        for (i in 0 until 5) {
            // 이벤트 발생!!, 버튼이 눌렸다고 가정
            count++
            callback() // 이벤트 발생 시점에 callback 호출 (여러 번)
        }

        continuation(count) // 모든 처리가 끝난 후 continuation 호출 (한 번)
    }
}

fun main() {
    CallbackExample.handleButtonClicked(
        callback = {
            log.info { "Button clicked" }
        },
        continuation = { count ->
            log.info { "Clicked count: $count" }
        }
    )
}
