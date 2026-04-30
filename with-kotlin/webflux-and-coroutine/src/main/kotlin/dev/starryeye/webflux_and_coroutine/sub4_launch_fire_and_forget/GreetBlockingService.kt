package dev.starryeye.webflux_and_coroutine.sub4_launch_fire_and_forget

/**
 * sub4 의 출발점이 되는 "변경 불가능하다고 가정하는" 외부 인터페이스.
 *
 * 가정
 *      이 인터페이스의 시그니처가 Unit (반환값 없음) 으로 굳어져 있고
 *      우리가 마음대로 바꿀 수 없다고 하자.
 *
 *      이 제약 안에서 "구현 안에서 suspend 함수를 호출하고 싶다" 는 것이 sub4 의 출발점이다.
 *      해결책은 같은 패키지의 [GreetBlockingServiceImpl] 참고.
 *
 *      sub2/sub3 와 같은 시리즈지만 반환 타입이 Unit 이고
 *      "결과를 받지 않는다 / 끝을 기다리지 않는다" 가 본질적으로 다르다.
 *      이 차이가 "권장하지 않음" 의 이유이기도 하다 ([Overview] section 3 참고).
 */
interface GreetBlockingService {
    fun findGreet()
}
