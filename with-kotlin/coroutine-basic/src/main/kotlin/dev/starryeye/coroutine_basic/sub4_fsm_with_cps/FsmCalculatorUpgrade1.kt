package dev.starryeye.coroutine_basic.sub4_fsm_with_cps

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

/**
 * FSM 에 CPS 접목하기 - 1차: 각각의 연산 함수에 CPS 적용
 *
 * 기존 FsmCalculator (sub2_finite_state_machine.FiniteStateMachine2) 에서는
 *      calculate 내부에서 `current.result += 1` 처럼 연산을 직접 수행했다.
 *
 * 여기서는
 *      각각의 연산 (initialize, addOne, multiplyTwo) 을 CPS 형태로 분리한다.
 *      값을 return 하는 대신 Continuation 을 인자로 받아 cont.resume(value) 로 결과를 전달한다.
 *
 * 각각의 연산 함수는 cont.resume 이 무엇을 하는지 모른다.
 *      단지 값을 계산한 뒤 전달받은 continuation 에 넘길 뿐이다.
 *          (sub3 에서 다룬 "callee 는 continuation 이 무엇인지 모른다" 와 동일)
 *
 * calculate 내부 흐름
 *      label 에 따라서 다른 연산 함수를 호출한다.
 *      각 case 에서
 *          Shared.result (이전 연산 결과) 를 꺼내 변수에 저장하여 활용한다.
 *          label 을 변경한다.
 *          연산 함수를 실행하면서 continuation 을 전달한다.
 *      3번 case 에 도달하면 Shared.result 를 log 로 출력하고 종료한다.
 *
 * calculate 가 생성하는 Continuation (cont)
 *      각 연산 함수는 cont.resume(value) 로 결과를 전달한다.
 *      cont.resumeWith 에서는
 *          결과값을 Shared.result 에 저장하고
 *          직접 재귀 호출을 하는 대신 this.calculate(...) 를 호출하여 transition 을 수행한다.
 *      즉, 각각의 연산자들은 cont.resume 이 무엇인지 몰랐지만
 *          결과적으로 연산을 수행하고 재귀 함수를 호출하게 된다.
 *
 * 실행 흐름 요약
 *      calculate(label=0) -> initialize -> cont.resume -> resumeWith -> calculate(label=1)
 *      calculate(label=1) -> addOne     -> cont.resume -> resumeWith -> calculate(label=2)
 *      calculate(label=2) -> multiplyTwo-> cont.resume -> resumeWith -> calculate(label=3)
 *      calculate(label=3) -> log + return
 */
private val log = KotlinLogging.logger {}

class FsmCalculatorUpgrade1 {

    data class Shared( // 이전 연산 결과 + 현재 상태(label) 를 담는다. (기존 FsmCalculator 와 동일)
        var result: Any? = null,
        var label: Int = 0,
    )

    fun calculate(initialValue: Int, shared: Shared? = null) {

        val current = shared ?: Shared()

        // 각 연산 함수가 결과를 전달해줄 continuation 를 익명 클래스로 구현
        val cont = object : Continuation<Int> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Int>) {
                // 연산 함수가 넘겨준 결과를 Shared 에 저장
                current.result = result.getOrThrow()

                // 연산 함수에서 재귀 호출을 하지 않고, resumeWith 에서 대신 호출하여 transition
                this@FsmCalculatorUpgrade1.calculate(initialValue, current)
            }
        }

        when (current.label) {
            0 -> {
                current.label = 1
                initialize(initialValue, cont)
            }

            1 -> {
                val initialized = current.result as Int
                current.label = 2
                addOne(initialized, cont)
            }

            2 -> {
                val added = current.result as Int
                current.label = 3
                multiplyTwo(added, cont)
            }

            3 -> {
                val multiplied = current.result as Int
                log.info { "Result: $multiplied" } // 여전히 log 가 하드코딩되어 있다 -> Upgrade2 에서 제거
                return
            }
        }
    }

    // 각각의 연산 함수는 결과 값을 return 하는 대신, 전달받은 continuation 에 resume 으로 결과 값을 넘긴다.
    private fun initialize(value: Int, cont: Continuation<Int>) {
        log.info { "Initial" }
        cont.resume(value)
    }

    private fun addOne(value: Int, cont: Continuation<Int>) {
        log.info { "Add one" }
        cont.resume(value + 1)
    }

    private fun multiplyTwo(value: Int, cont: Continuation<Int>) {
        log.info { "Multiply two" }
        cont.resume(value * 2)
    }
}

fun main() {
    FsmCalculatorUpgrade1().calculate(5)
}
