package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub4_relationship

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * CoroutineScope, CoroutineContext, Continuation, Coroutine, Coroutine builder 의 관계
 *
 * 한 줄 정리
 *      Continuation       -> CoroutineContext 를 가진다
 *      CoroutineScope     -> CoroutineContext 를 가진다
 *      CoroutineContext   -> Element 들의 묶음 (Job, Dispatcher, CoroutineName, ...)
 *      Job                -> CoroutineContext.Element 의 한 종류
 *      AbstractCoroutine  -> Job, Continuation, CoroutineScope 셋을 동시에 구현하는 런타임 클래스
 *                            (BlockingCoroutine, StandaloneCoroutine 등이 이걸 상속)
 *      Coroutine Builder  -> AbstractCoroutine 후손을 만들어서 시작시키는 "함수들" 의 통칭
 *                            launch, async, runBlocking, coroutineScope, withContext, produce, flow ...
 *
 *
 * Continuation 과 CoroutineScope 의 관계
 *
 *      둘 다 CoroutineContext 를 들고 있지만 "다른 계층의 추상화" 이다.
 *      각자 다른 목적으로 context 를 들고 있을 뿐이다.
 *
 *      특징
 *          - Continuation   = "어떻게 재개되는가"  (낮은 레벨, Dispatcher 가 사용)
 *          - CoroutineScope = "어디에서 띄우는가"  (높은 레벨, 애플리케이션 코드가 사용)
 *
 *      흐름
 *          [애플리케이션 코드]
 *             ↓ scope.launch { ... }              <- CoroutineScope 영역에서 시작 ("어디서")
 *          [builder]
 *             ↓ 새 AbstractCoroutine 생성
 *             ↓ 그 안에 Continuation 도 같이 들어있음
 *          [Dispatcher]
 *             ↓ continuation.resumeWith(...)      <- Continuation 으로 깨움 ("어떻게")
 *          [block 실행 → 끝나면 Job 완료]
 *
 *      요약
 *          CoroutineScope 는 "시작점" 역할, Continuation 은 "재개점" 역할.
 *          같은 coroutine 객체의 두 얼굴이지만 외부에서 보기엔 누가 부르느냐(코드 vs 런타임) 가 다르다.
 *
 *
 * Coroutine Builder
 *      launch / async / runBlocking 같은 함수들을 묶어 부르는 관용적 호칭.
 *
 *      동작
 *          1) CoroutineContext 를 인자로 받아 부모 context 와 merge
 *          2) 새 AbstractCoroutine 후손 객체를 생성 (= 새 Job 도 같이 생성)
 *          3) 부모 Job 에 자식으로 매단다 (structured concurrency)
 *          4) block(suspend 람다) 을 실행 (Dispatcher 에 따라 즉시/지연)
 *
 *      대표 builders
 *          ┌──────────────────┬───────────────────────┬──────────────────────────────────────────┐
 *          │ builder          │     AbstractCoroutine │ 반환                                      │
 *          ├──────────────────┼───────────────────────┼──────────────────────────────────────────┤
 *          │ launch           │ StandaloneCoroutine   │ Job                                      │
 *          │ async            │ DeferredCoroutine     │ Deferred<T> (Job 의 자식)                  │
 *          │ runBlocking      │ BlockingCoroutine     │ T (현재 스레드를 blocking)                   │
 *          │ coroutineScope   │ ScopeCoroutine        │ T (suspend, 자식 다 끝날 때까지 대기)          │
 *          │ withContext      │ UndispatchedCoroutine │ T (suspend, context 만 갈아끼움)            │
 *          └──────────────────┴───────────────────────┴──────────────────────────────────────────┘
 *
 *      종류
 *          - CoroutineScope 의 확장함수
 *                  launch, async
 *                      -> CoroutineScope 가 있어야만 호출 가능 (Structured concurrency 강제)
 *          - 일반 (top-level) 함수
 *                  runBlocking, coroutineScope, withContext
 *                      -> 자기 자신이 새 scope 를 만들어 block 의 receiver 로 제공
 */
private val log = KotlinLogging.logger {}

fun main() = runBlocking {
    
}
