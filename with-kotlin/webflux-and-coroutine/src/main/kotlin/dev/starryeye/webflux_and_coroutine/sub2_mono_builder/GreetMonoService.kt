package dev.starryeye.webflux_and_coroutine.sub2_mono_builder

import reactor.core.publisher.Mono

/**
 * sub2 의 출발점이 되는 "변경 불가능하다고 가정하는" 외부 인터페이스.
 *
 * 가정
 *      이 인터페이스는 외부 라이브러리에서 제공된다고 치자.
 *      또는 이미 너무 많은 곳에서 호출되고 있어 시그니처를 suspend 로 바꾸기 어렵다고 하자.
 *      즉, 반환 타입(Mono<String>) 을 우리가 마음대로 바꿀 수 없다는 제약이 있다.
 *
 *      이 제약 안에서 "구현 안에서 suspend 함수를 호출하고 싶다" 는 것이 sub2 의 출발점이다.
 *      해결책은 같은 패키지의 [GreetMonoServiceImpl] 참고.
 */
interface GreetMonoService {
    fun findGreet(): Mono<String>
}
