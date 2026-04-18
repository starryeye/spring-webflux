package dev.starryeye.coroutine_basic.sub2_finite_state_machine

import io.github.oshai.kotlinlogging.KotlinLogging

/**
 *
 * 아래는 FSM(Finite state machine) 을 재귀 함수를 이용해서 구현한 예시이다.
 *      각각의 state 에 따라 다른 코드를 실행
 *      재귀 함수가 수행될 때마다 state 가 변화한다.
 *
 * 예제
 *      입력값을 기준으로 (입력값 + 1) * 2 를 계산한다.
 *
 * NormalCalculator 는 FSM 을 사용하지 않은 익숙한 코드
 * FsmCalculator 는 FSM 을 이용
 */
private val log = KotlinLogging.logger {}

fun main() {
    NormalCalculator.calculate(5)
    FsmCalculator.calculate(5)
}

object NormalCalculator {

    fun calculate(value: Int) {
        var result = value
        result += 1
        result *= 2
        log.info { "normal calculator result: $result" }
    }
}

object FsmCalculator {

    data class State( // 현재 상태 역할을 한다.
        var result: Int = 0,
        var label: Int = 0,
    )

    fun calculate(value: Int, state: State? = null) {

        val current = state ?: State()

        when (current.label) {
            0 -> {
                current.result = value
                current.label = 1
            }

            1 -> {
                current.result += 1
                current.label = 2
            }

            2 -> {
                current.result *= 2
                current.label = 3
            }

            3 -> {
                log.info { "FSM Calculator result: ${current.result}" }
                return
            }
        }

        // transition
        this.calculate(value, current) // 재귀 함수 호출, 현재 상태를 넘긴다.
    }
}