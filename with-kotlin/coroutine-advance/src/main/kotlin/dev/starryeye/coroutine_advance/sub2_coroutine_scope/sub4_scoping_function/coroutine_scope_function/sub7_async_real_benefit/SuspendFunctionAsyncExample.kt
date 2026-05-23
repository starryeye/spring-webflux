package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.coroutine_scope_function.sub7_async_real_benefit

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * suspend 함수 + async + coroutineScope 실전 패턴 (이점 1)
 *
 *
 * 시나리오
 *      "사용자 정보 (이름 + 점수) 를 두 군데서 동시에 가져와 하나로 합쳐 돌려주는 함수" 를 만든다.
 *      각 호출이 100ms 걸린다고 가정.
 *          - 동시 (병렬) 로 부르면 약 100ms
 *          - 순차로 부르면 약 200ms
 *      이런 함수를 가장 자연스럽게 만들 때 코드 모양을 본다.
 *
 *
 * 왜 coroutineScope 가 필요한가
 *      fetchUser() 같은 평범한 suspend 함수 본문에는 CoroutineScope receiver 가 없다.
 *      그래서 그 본문 안에서 async 를 직접 부르면 컴파일이 안 된다.
 *      coroutineScope { ... } 가 그 receiver 를 한정된 블록 동안 빌려준다.
 *
 *
 * 핵심 포인트
 *      1) 함수 본문이 = coroutineScope { ... } 한 줄로 시작.
 *          - coroutineScope 가 block 의 마지막 표현 값 (User 객체) 을 그대로 반환.
 *          - 호출자는 그냥 "suspend fun fetchUser(): User" 시그니처만 보면 된다 — 내부에 async 가 있는지 알 필요 없음.
 *      2) 두 async 가 거의 같은 순간에 시작 → 약 100ms 만에 두 결과를 다 받는다 (병렬).
 *          - 순차로 짰다면 fetchName(); fetchScore() 가 차례로 돌아 200ms 가 걸렸을 흐름.
 *      3) 안전성 (보이지는 않지만)
 *          - 만약 fetchScore() 가 예외를 던지면, coroutineScope 가 fetchName() 도 자동으로 cancel.
 *          - 호출자는 평범한 try-catch 로 fetchUser() 만 감싸면 끝.
 *
 *
 * 출력
 *      [main @coroutine#1] - fetched: User(name=Alice, score=42)   (전체 약 100ms 안에 출력)
 */
private val log = KotlinLogging.logger {}

private data class User(
    val name: String,
    val score: Int,
)

private suspend fun fetchName(): String {
    delay(timeMillis = 100)
    return "Alice"
}

private suspend fun fetchScore(): Int {
    delay(timeMillis = 100)
    return 42
}

/**
 * 일반 suspend 함수 안에서 async 두 개를 띄워 병렬 호출 후 결과를 합쳐 돌려주는 전형 패턴.
 *
 *      - coroutineScope 가 없으면 본문 안에서 async 호출 자체가 안 된다 (컴파일 에러).
 *      - 호출자 입장에서는 그냥 평범한 suspend fun fetchUser(): User. 내부의 병렬 구성은 숨겨진다.
 */
private suspend fun fetchUser(): User = coroutineScope {
    val name = async { fetchName() }
    val score = async { fetchScore() }
    User(name = name.await(), score = score.await())
}

fun main() {
    runBlocking {
        val user = fetchUser()
        log.info { "fetched: $user" }
    }
}
