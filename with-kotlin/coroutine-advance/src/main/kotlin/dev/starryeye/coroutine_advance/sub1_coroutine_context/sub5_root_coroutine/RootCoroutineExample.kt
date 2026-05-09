package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub5_root_coroutine

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Root Coroutine 이란
 *
 * 정의
 *      "coroutine 의 부모 Job 이 '다른 coroutine 의 Job 이 아닌' 경우, 그 coroutine 을 root coroutine 이라고 한다."
 *
 *          - 부모가 그냥 Job (CoroutineScope 의 JobImpl, SupervisorJob 등)   -> 이 coroutine 은 root
 *          - 부모가 또 다른 coroutine (StandaloneCoroutine, BlockingCoroutine 등) -> non-root
 *
 *
 * 코드상으로 구분해보기
 *      바로 위가 builder(launch/async/runBlocking) "안" 이면 non-root,
 *      scope 객체에 점 찍고 launch 한 거면 root 다.
 *
 *          runBlocking {                       // BlockingCoroutine = root
 *              launch { }                       // non-root (안쪽이라서)
 *              launch {                         // non-root
 *                  launch { }                   // non-root (또 안쪽)
 *              }
 *
 *              CoroutineScope(...).launch { }  // root (점 찍고 시작 -> 새 scope 의 직접 자식)
 *              GlobalScope.launch { }           // root
 *          }
 *
 *
 * 패턴 표
 *      ┌──────────────────────────────────────────────────┬──────────────────┬───────────────────┐
 *      │ 부모 Job 의 정체                                    │ 그 자식 launch 는? │ Handler 동작?       │
 *      ├──────────────────────────────────────────────────┼──────────────────┼───────────────────┤
 *      │ CoroutineScope(Job()) 의 JobImpl                  │ root              │ O                 │
 *      │ GlobalScope 의 Job                                │ root              │ O                 │
 *      │ 다른 launch 가 만든 StandaloneCoroutine             │ non-root          │ X (부모로 전파)     │
 *      │ runBlocking 의 BlockingCoroutine                  │ non-root          │ X                 │
 *      │ SupervisorJob / supervisorScope 의 Job            │ root처럼 동작       │ O                 │
 *      └──────────────────────────────────────────────────┴──────────────────┴───────────────────┘
 *
 *      이 표가 그대로 CoroutineExceptionHandler 가 동작하는지 안 하는지의 기준이 된다
 *      (-> sub3_elements/sub6_coroutine_exception_handler 참고).
 *
 *
 * 디버깅으로 확인하기
 *      엄밀한 public API 는 없지만 부모 Job 의 실제 클래스를 찍어보면 알 수 있다.
 *          - parent::class.simpleName 이 *Coroutine (StandaloneCoroutine, BlockingCoroutine ...) 이면 non-root
 *          - JobImpl, SupervisorJobImpl 등 일반 Job 구현체이면 root
 *
 *      이 예제 main() 에서 그 차이를 출력해 본다.
 *
 *
 * 출력 예시
 *      [main @coroutine#1] - [scope.launch]       me = StandaloneCoroutine, parent = JobImpl
 *      [main @coroutine#1] - [scope.launch.launch] me = StandaloneCoroutine, parent = StandaloneCoroutine
 *      [main @coroutine#1] - [runBlocking]         me = BlockingCoroutine, parent = null
 *      [main @coroutine#1] - [runBlocking.launch] me = StandaloneCoroutine, parent = BlockingCoroutine
 */
private val log = KotlinLogging.logger {}

@OptIn(ExperimentalCoroutinesApi::class)   // Job.parent 가 @ExperimentalCoroutinesApi 라서 opt-in 필요
private fun describe(label: String, me: Job) {
    val parent = me.parent
    log.info { "[$label] me = ${me::class.simpleName}, parent = ${parent?.let { it::class.simpleName }}" }
}

fun main() {
    runBlocking {
        // (1) runBlocking 의 람다 자체 = root (BlockingCoroutine)
        //      그 부모는 없음 (null) -> "부모가 다른 coroutine 이 아니다" -> root
        describe(label = "runBlocking", me = coroutineContext[Job]!!)

        // (2) runBlocking 안에서 그냥 launch -> non-root
        //      부모가 BlockingCoroutine (= 다른 coroutine) 이라서.
        launch {
            describe(label = "runBlocking.launch", me = coroutineContext[Job]!!)
        }.join()

        // (3) CoroutineScope(...) 팩토리 + .launch -> root
        //      이 scope 의 Job 은 그냥 JobImpl (coroutine 아님) 이라서.
        val outerScope = CoroutineScope(context = Dispatchers.IO + Job())
        outerScope.launch {
            describe(label = "scope.launch", me = coroutineContext[Job]!!)

            // (4) 그 안의 또 launch -> non-root
            //      부모가 StandaloneCoroutine (위의 scope.launch) 이라서.
            launch {
                describe(label = "scope.launch.launch", me = coroutineContext[Job]!!)
            }.join()
        }.join()
    }
}
