package dev.starryeye.coroutine_basic.sub8_coroutine_converted_by_kotlin_compiler

/**
 * sub8 - Coroutine으로 변경하기
 *
 *
 * sub6 와의 관계
 *      sub6 의 OrderAsyncExampleUpgrade2 는
 *          "컴파일러가 suspend 함수를 내부적으로 어떤 모양(FSM + CPS + Continuation)으로 바꾸는가"
 *          를 손으로 쓴 버전이다.
 *      sub8 는 그 코드를 다시 사람이 읽는 suspend 코드로 되돌린 최종 형태를 보여준다.
 *
 * 이 챕터의 핵심 변환
 *      1. when(label) 기반 state machine 제거
 *      2. cont.result / cont.customer / cont.products 같은 중간 상태 제거
 *      3. completion.resume(...) 대신 return 사용
 *      4. source 코드의 explicit Continuation 파라미터 제거
 *      5. 최종 함수에 suspend 와 반환 타입 추가
 *
 * 결과적으로 source code 에서는 선형 코드만 보이지만,
 *      실제로는 compiler 가 숨겨진 Continuation 과 state machine 을 다시 만들어준다.
 *      즉 sub6 의 복잡도가 사라진 것이 아니라 compiler 뒤로 이동한 것이다.
 *
 *
 * 정리1.
 * Kotlin complier 는 suspend 가 붙은 함수에 Continuation 인자를 추가한다. (CPS)
 * 다른 suspend 함수를 실행하면 소유하고 있는 Continuation 을 같이 전달한다.
 *      이러한 변환으로 인해 전달할 Continuation 이 없으므로 suspend 가 없는 함수에서 다른 suspend 함수 호출이 불가한 것이다.
 *
 * 정리2.
 * Kotlin complier 는 suspend 함수 내부를 when 문을 이용해서 FSM 상태로 변경한다.
 *      각각의 state 에서는 label 을 변경하고 비동기 함수를 수행한다.
 *      비동기 함수가 완료되면 continuation.resume 을 수행하여 다시 복귀
 *      하지만, label 이 변경되면서 다른 state 로 transition
 *      마지막 state 에 도달하면 completion.resume 을 수행하고 종료된다.
 */
