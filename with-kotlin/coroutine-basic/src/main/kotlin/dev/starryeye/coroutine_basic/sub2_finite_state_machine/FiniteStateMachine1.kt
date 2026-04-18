package dev.starryeye.coroutine_basic.sub2_finite_state_machine

import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * State machine
 *      상태를 표현하는 모델이다.
 *          CS 영역에서 자주 접하는 라이프 사이클이라 생각하면 편함
 *      구성 요소
 *          state : 시스템의 특정 상황
 *          transition : 하나의 state 에서 다른 state 로 이동 수단
 *          event : transition 을 트리거하는 외부의 사건
 *      요약
 *          상태 A는 외부 Event 를 통해 트리거된 transition 으로 상태 B가 된다.
 *
 * [state A] ----transition----> [state B]
 *
 * Finite state machine
 *      유한한 개수의 state 를 가지는 State machine 이다.
 *      제약 사항
 *          한번에 하나의 state 만 해당될 수 있다. (동시에 두개의 상태가 될 수는 없다.)
 *          Event 를 통해서 하나의 state 에서 다른 state 로 transition 이 가능하다.
 *
 * 아래는 FSM(Finite state machine) 을 재귀 함수를 이용해서 구현한 예시이다.
 *      각각의 state 에 따라 다른 코드를 실행
 *      재귀 함수가 수행될 때마다 state 가 변화한다.
 *
 */
private val log = KotlinLogging.logger {}

class FsmExample {

    fun execute(label: Int = 0) { // label은 현재상태, nextLabel은 다음 상태
        var nextLabel: Int? = null

        when (label) { // label 변수를 이용하여 when 으로 해당 상태에 맞게 작업을 수행
            0 -> {
                log.info {"Initial"}
                nextLabel = 1
            }
            1 -> {
                log.info {"state 1"}
                nextLabel = 2
            }
            2 -> {
                log.info {"state 2"}
                nextLabel = 3
            }
            3 -> {
                log.info {"end"}
            }
        }

        // transition
        if (nextLabel != null) {
            this.execute(nextLabel) // 재귀 함수를 호출할때 nextLabel 을 전달해서 상태를 변경한다.
        }
    }
}

fun main() {
    val fsmExample = FsmExample()
    fsmExample.execute()
}