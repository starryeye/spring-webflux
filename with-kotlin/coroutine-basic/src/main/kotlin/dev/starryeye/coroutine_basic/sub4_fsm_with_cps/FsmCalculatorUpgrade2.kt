package dev.starryeye.coroutine_basic.sub4_fsm_with_cps

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

/**
 * FSM 에 CPS 접목하기 - 2차: calculate 함수에도 CPS 적용
 *
 * FsmCalculatorUpgrade1 에서 남아있던 문제
 *      결과를 찾은 후 log 를 하는 부분이 하드코딩되어 있다.
 *      Shared 객체와 Continuation 이 따로 존재한다.
 *
 * Upgrade2 에서의 변화
 *      Shared 객체 대신 Continuation 구현체(CustomContinuation) 를 활용한다.
 *          label, result 를 CustomContinuation 이 직접 가진다.
 *      log.info 로 직접 출력하던 부분을 제거하고, main 이 전달한 completion continuation 을 호출한다.
 *      main 에서 "모든 작업이 끝난 뒤 수행할 Continuation" 을 직접 생성해서 calculate 에 넘긴다.
 *
 * CustomContinuation
 *      Continuation<Int> 인터페이스를 구현한다.
 *      가장 마지막에 호출될 completion Continuation 과 FsmCalculatorUpgrade2 인스턴스를 인자로 받는다.
 *      resumeWith 에서는
 *          result 를 자신의 필드에 저장하고
 *          that.calculate(0, this) 로 재귀 호출하여 transition 을 수행한다.
 *      complete(value) 는 completion.resume(value) 를 호출하여 전체 흐름을 종료한다.
 *      private class 로 선언하여 외부에서는 생성할 수 없다.
 *
 * calculate 흐름
 *      인자로 받은 continuation 이 CustomContinuation 인지 확인한다.
 *          CustomContinuation 이 아니라면 -> main 이 최초로 넘긴 completion 이므로, CustomContinuation 으로 감싼다.
 *          CustomContinuation 이라면     -> resumeWith 를 통해 재귀적으로 전달된 것이므로 그대로 사용한다.
 *      각 case 에서 Shared 객체 대신 CustomContinuation 의 label 과 result 를 이용한다.
 *      마지막 state 에서는 직접 log 하지 않고 cont.complete(multiplied) 를 호출하여
 *          main 의 completion 으로 제어를 넘긴다.
 *
 * 정리
 *      CPS 를 통해서 모든 함수가 continuation 을 인자로 전달받고 resume 을 수행하게 되었다.
 *      calculate 함수는 Continuation 인터페이스를 활용하여
 *          main 이 전달하는 completion 과
 *          스스로가 전달하는 CustomContinuation
 *          을 모두 수용한다.
 *      마지막 state 가 아니라면 CustomContinuation 의 resume 을 수행하여 transition 하고,
 *      마지막 state 라면 main 의 completion 을 수행하여 종료한다.
 *
 * 의미
 *      이 구조가 바로 Kotlin compiler 가 suspend 함수를 변환하는 기본 골격이다.
 *      즉, 우리가 작성한 선형적인 suspend 코드는
 *          컴파일 시 이런 식의 (FSM + CPS + CustomContinuation) 형태로 변환된다.
 */
private val log = KotlinLogging.logger {}

class FsmCalculatorUpgrade2 {

    private class CustomContinuation(
        val completion: Continuation<Int>, // main 이 전달한, 가장 마지막에 호출될 continuation
        val that: FsmCalculatorUpgrade2,   // resumeWith 에서 재귀 호출할 대상
    ) : Continuation<Int> {                // Shared 의 역할(label, result) 을 CustomContinuation 이 직접 담당

        var result: Any? = null
        var label: Int = 0

        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Int>) {
            this.result = result.getOrThrow()
            // calculator 가 직접 재귀 호출 대신, continuation 의 resumeWith 에서 대신 호출하여 transition
            that.calculate(0, this)
        }

        // 최종 결과를 main 이 넘긴 completion 으로 넘겨서 전체 흐름을 종료시킨다.
        fun complete(value: Int) {
            completion.resume(value)
        }
    }

    fun calculate(
        initialValue: Int,
        continuation: Continuation<Int>,
    ) {
        // 최초 진입이라면 main 이 넘긴 completion 을 CustomContinuation 으로 감싼다.
        // 재귀 진입이라면 이미 CustomContinuation 이므로 그대로 사용한다.
        //      (CustomContinuation 은 private class 이므로, 외부에서 넘어온 continuation 은 절대 CustomContinuation 일 수 없다.)
        val cont = if (continuation is CustomContinuation) {
            continuation
        } else {
            CustomContinuation(continuation, this)
        }

        when (cont.label) {
            0 -> {
                cont.label = 1
                initialize(initialValue, cont)
            }

            1 -> {
                val initialized = cont.result as Int
                cont.label = 2
                addOne(initialized, cont)
            }

            2 -> {
                val added = cont.result as Int
                cont.label = 3
                multiplyTwo(added, cont)
            }

            3 -> {
                val multiplied = cont.result as Int
                cont.complete(multiplied) // 직접 log 하지 않고 main 의 completion 으로 제어 이관
            }
        }
    }

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
    // main 에서 "모든 작업이 끝난 뒤 실행할 continuation" 을 직접 만들어서 전달한다.
    // Continuation(context, lambda) 는 Continuation 인터페이스를 간편하게 구현해주는 factory 함수이다.
    //      lambda 시그니처는 (Result<T>) -> Unit 이므로 it 은 Result<Int> 이다.
    val completion = Continuation<Int>(EmptyCoroutineContext) {
        log.info { "Result: ${it.getOrThrow()}" }
    }
    FsmCalculatorUpgrade2().calculate(5, completion)
}
