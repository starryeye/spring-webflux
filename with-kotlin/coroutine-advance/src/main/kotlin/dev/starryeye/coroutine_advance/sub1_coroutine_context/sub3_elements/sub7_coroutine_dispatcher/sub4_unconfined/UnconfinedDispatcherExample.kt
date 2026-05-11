package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub4_unconfined

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * CoroutineDispatcher Unconfined
 *
 * 핵심
 *      - Unconfined 는 "특정 스레드에 묶이지 (confine) 않는다" 는 의미의 dispatcher 다.
 *      - 즉, Default / IO / Main 처럼 "자기 소유의 스레드 풀" 을 가지고 있지 않다.
 *      - 그래서 Unconfined 로 설정된 coroutine 이 어떤 스레드에서 실행될지 예상하기 힘들다.
 *
 *
 * 동작 규칙
 *      1) 시작 시점
 *          - coroutine builder (launch / async ...) 가 호출되면
 *              "호출한 (caller) 스레드" 에서 그대로 즉시 실행된다.
 *          - dispatch 를 하지 않고, 호출 스택 위에서 그대로 본문이 inline 처럼 시작된다는 의미.
 *
 *      2) 첫 suspend 이후 (= resume 시점)
 *          - suspend 함수가 한 번 멈췄다가 재개되면,
 *              "그 suspend 함수를 resume 시켜준 스레드" 에서 그대로 이어진다.
 *          - 다른 말로 하면, "마지막으로 실행된 suspend 함수의 resume 스레드" 를 따라간다.
 *          - 예) withContext(Dispatchers.IO) 안에서 작업을 하다가 빠져나오면,
 *              비록 dispatcher 가 Unconfined 라 하더라도 IO worker thread 위에서 그대로 이어진다.
 *          - 예) delay(...) 가 끝나고 resume 되면 delay 를 깨워준 kotlinx.coroutines.DefaultExecutor
 *              스레드 위에서 그대로 이어진다 (delay 는 내부적으로 DefaultExecutor 의 timer 가 resume 함).
 *
 *
 * 그래서 왜 위험한가
 *      - "어디서 실행될지 호출 흐름을 따라가 보지 않으면 알 수 없다."
 *      - 일반 비즈니스 로직에서 쓰면, 어느 순간 갑자기 Default worker / IO worker / DefaultExecutor 위에서
 *          내 코드가 돌고 있을 수 있다. 디버깅 / 성능 분석 / context 누수 모두 추적이 어렵다.
 *      - 그래서 공식 문서에도 "일반적인 코드에서는 사용하면 안 된다" 고 명시되어 있다.
 *
 *      그럼 언제 쓰나
 *          - 라이브러리 내부에서 즉시 (호출 스레드에서) 실행되는 효과를 원할 때,
 *              혹은 특정 테스트 (특히 동기적 결과 검증) 에서 dispatch 비용을 없애고 싶을 때 정도로 제한적.
 *
 *          대표 사례: Spring WebFlux 의 suspend controller 진입점
 *              - WebFlux 는 suspend 로 선언된 controller method 를 호출할 때 내부적으로
 *                  org.springframework.core.CoroutinesUtils.invokeSuspendingFunction(...) 을 거치는데,
 *                  여기서 기본 CoroutineContext 가 Dispatchers.Unconfined 다.
 *                  (대략 mono(Dispatchers.Unconfined) { method.callSuspend(...) } 형태)
 *
 *              - 왜 Unconfined 인가?
 *                  WebFlux 는 이미 Reactor 의 event loop thread (예: reactor-http-nio-N) 위에서 요청을 흘려보내고 있다.
 *                  여기서 framework 가 임의로 Default / IO 같은 dispatcher 로 옮겨버리면
 *                      (a) 불필요한 thread 전환 비용이 생기고,
 *                      (b) 더 중요하게는 Reactor scheduler 가 결정한 스레드 흐름이 끊긴다.
 *                  Unconfined 는 "현재 caller 스레드에서 그대로 시작하고, 이후엔 suspend 가 resume 시킨 스레드를 따라간다" 이므로,
 *                  결국 Reactor 가 정해주는 thread 분배 (예: subscribeOn / publishOn / netty 의 event loop) 에 자연스럽게 얹힌다.
 *                  -> framework boundary 에서 "내가 dispatcher 를 강제로 정하지 않을 테니, 호출자(Reactor) 의 스레드 흐름을 그대로 따르라" 는 의미.
 *
 *              - 우리가 controller 본문에서 직접 Dispatchers.Unconfined 를 쓰는 일은 거의 없다.
 *                  이건 framework 진입점/어댑터 (suspend ↔ Reactor 변환) 에서나 의미가 있는 패턴.
 *                  비즈니스 코드에서는 여전히 IO / Default 를 의도에 맞게 골라 써야 한다.
 *
 *
 * 이 예제의 흐름과 출력
 *      runBlocking { launch(Dispatchers.Unconfined) { ... } }
 *
 *      [1] launch 직후 (아직 suspend 한 번도 안 함)
 *              -> caller 스레드 (runBlocking 의 main 스레드) 에서 그대로 실행
 *              -> thread1: main @coroutine#2
 *
 *      [2] withContext(Dispatchers.IO) 진입
 *              -> withContext 가 dispatcher 를 IO 로 바꾸면서 dispatch
 *              -> thread in withContext: DefaultDispatcher-worker-1 @coroutine#2
 *
 *      [3] withContext 종료 후
 *              -> 원래 dispatcher 는 Unconfined 이므로 "되돌릴 스레드" 가 없다.
 *              -> "마지막 resume 스레드" 를 그대로 따라가서 여전히 worker-1 위
 *              -> thread2: DefaultDispatcher-worker-1 @coroutine#2
 *
 *      [4] delay(100) 후
 *              -> delay 는 DefaultExecutor 의 timer thread 가 100ms 뒤 resume 시켜준다.
 *              -> Unconfined 는 이걸 그대로 따라가므로 thread 가 DefaultExecutor 로 옮겨간다.
 *              -> thread3: kotlinx.coroutines.DefaultExecutor @coroutine#2
 *
 *
 * 출력 예시
 *      [main @coroutine#2]                              - thread1: main @coroutine#2
 *      [DefaultDispatcher-worker-1 @coroutine#2]        - thread in withContext: DefaultDispatcher-worker-1 @coroutine#2
 *      [DefaultDispatcher-worker-1 @coroutine#2]        - thread2: DefaultDispatcher-worker-1 @coroutine#2
 *      [kotlinx.coroutines.DefaultExecutor @coroutine#2] - thread3: kotlinx.coroutines.DefaultExecutor @coroutine#2
 *
 *
 * 한 줄 요약
 *      - 처음엔 caller 스레드, 그 뒤로는 "마지막 suspend 함수가 resume 시킨 스레드" 를 그대로 따라가는 dispatcher.
 *      - 예측 불가능하므로 일반 코드에서는 사용 금지.
 */
private val log = KotlinLogging.logger {}

private fun threadName(): String = Thread.currentThread().name

fun main() {
    runBlocking {
        launch(context = Dispatchers.Unconfined) {
            // [1] caller (runBlocking 의 main) 스레드에서 그대로 시작
            log.info { "thread1: ${threadName()}" }

            // [2] withContext 로 잠깐 IO dispatcher 위로 옮겨감
            withContext(context = Dispatchers.IO) {
                log.info { "thread in withContext: ${threadName()}" }
            }

            // [3] withContext 를 빠져나온 뒤, Unconfined 는 "원래 스레드로 되돌리지" 않고
            //      마지막 resume 스레드(= IO worker) 를 그대로 따라간다.
            log.info { "thread2: ${threadName()}" }

            // [4] delay 는 내부적으로 DefaultExecutor timer thread 가 resume 시켜주므로
            //      이후의 스레드는 DefaultExecutor 로 옮겨간다.
            delay(timeMillis = 100)
            log.info { "thread3: ${threadName()}" }
        }
    }
}
