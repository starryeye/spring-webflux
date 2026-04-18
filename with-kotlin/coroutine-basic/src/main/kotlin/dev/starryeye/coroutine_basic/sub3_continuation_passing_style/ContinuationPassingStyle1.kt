package dev.starryeye.coroutine_basic.sub3_continuation_passing_style

import io.github.oshai.kotlinlogging.KotlinLogging

/**
 *
 * Direct style
 *      Caller 가 callee 를 호출하는 상황에서
 *          callee 는 값을 계산하여 반환한다.
 *          caller 는 callee 가 반환한 결과를 사용한다.
 *      일반적인 동기 코드에서 사용하는 익숙한 스타일이다.
 *
 * Continuation passing style (CPS)
 *      Direct style 의 반대되는 스타일이다.
 *      함수의 결과를 반환(return) 하는 대신,
 *          "다음에 무엇을 할지(continuation)" 를 인자로 넘겨받아 마지막에 그 continuation 을 호출하는 스타일이다.
 *      Caller 가 callee 를 호출하는 상황에서
 *          callee 는 값을 계산하여 continuation 을 실행하고 인자로 값을 전달한다.
 *          continuation 은 callee 가장 마지막에서 딱 한 번 실행된다.
 *      Continuation
 *          자체 상태가 없는 함수일 뿐이다.
 *          callee 가 실행시켜준다.
 *
 * 예제
 *      입력값을 기준으로 (입력값 + 1) * 2 를 계산한다.
 *
 * NormalSeparatedCalculator 는 Direct style (return 값을 그대로 사용)
 * CpsCalculator 는 CPS style (return 대신 continuation 을 호출)
 */
private val log = KotlinLogging.logger {}

fun main() {
    NormalSeparatedCalculator.calculate(5)
    CpsCalculator.calculate(5) { result ->
        log.info { "CPS Calculator result: $result" }
    }
}

object NormalSeparatedCalculator {

    fun calculate(initialValue: Int) {
        var result = initialize(initialValue)
        result = addOne(result)
        result = multiplyTwo(result)
        log.info { "Normal Separated Calculator result: $result" }
    }

    private fun initialize(value: Int): Int {
        return value
    }

    private fun addOne(value: Int): Int {
        return value + 1
    }

    private fun multiplyTwo(value: Int): Int {
        return value * 2
    }
}

object CpsCalculator {

    // 최종적으로 결과를 받아서 처리할 continuation 을 인자로 받는다. (return 타입은 Unit)
    fun calculate(initialValue: Int, continuation: (Int) -> Unit) {

        // 아래 로직은 한줄로 봐야한다.
        initialize(initialValue) { initial -> // initialize 의 결과(initial)를 다음 continuation 으로 받는다.
            addOne(initial) { added ->
                multiplyTwo(added) { multiplied ->
                    continuation(multiplied) // 모든 계산이 끝난 뒤, 최초에 전달받은 continuation 을 마지막에 1회 호출
                }
            }
        }
    }

    private fun initialize(value: Int, continuation: (Int) -> Unit) {
        log.info { "Initial" }
        continuation(value) // return 대신 continuation 을 호출하여 값을 넘긴다. (continuation 을 실행하는 것은 곧 람다를 실행하는 것)
    }

    private fun addOne(value: Int, continuation: (Int) -> Unit) {
        log.info { "Add one" }
        continuation(value + 1)
    }

    private fun multiplyTwo(value: Int, continuation: (Int) -> Unit) {
        log.info { "Multiply two" }
        continuation(value * 2)
    }
}
