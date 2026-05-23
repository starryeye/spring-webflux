package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.coroutine_scope_function.sub3_coroutine_scope_function

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

/**
 * coroutineScope 와 launch 의 차이
 *
 *
 * 차이 1) 동기 vs 비동기
 *      - coroutineScope { ... } 는 suspend 함수.
 *          - caller 가 block 끝까지 그 자리에서 suspend 했다가 resume.
 *          - 다음 줄은 block 이 끝난 뒤에 실행된다.
 *      - launch { ... } 는 builder.
 *          - 호출 즉시 Job 만 반환하고 caller 는 그대로 다음 줄로 진행.
 *          - block 의 완료를 기다리려면 직접 .join() 호출 필요.
 *
 *
 * 차이 2) Coroutine 인스턴스 / CoroutineId 처리
 *      - coroutineScope 는 ScopeCoroutine 을 만들지만, 부모의 CoroutineId 를 그대로 이어 받는다.
 *          → 출력에서 root 의 "coroutine#2" 가 coroutineScope 안에서도 "coroutine#2" 로 그대로 유지.
 *      - launch 는 새 StandaloneCoroutine 을 만들고 새로운 CoroutineId 가 부여된다.
 *          → 출력에서 새로운 "coroutine#3" 이 등장.
 *      - 즉 coroutineScope 는 "같은 흐름의 연장선", launch 는 "새 흐름의 분기" 라는 느낌.
 *
 *
 * 차이 3) 반환값
 *      - coroutineScope 는 block 의 마지막 표현 값을 그대로 R 로 반환한다.
 *          - 예제에선 getResult() 의 반환값 100 이 그대로 result 변수에 들어옴.
 *      - launch 는 결과값 없음. Job 만 반환.
 *          - 결과가 필요한 비동기 작업은 async + await 으로 가야 한다.
 *
 *
 * 출력
 *      [DefaultDispatcher-worker-1 @coroutine#2] - context in root: [CoroutineId(2), "coroutine#2":StandaloneCoroutine{Active}@..., Dispatchers.Default]
 *      [DefaultDispatcher-worker-1 @coroutine#2] - context in coroutineScope: [CoroutineId(2), "coroutine#2":ScopeCoroutine{Active}@..., Dispatchers.Default]
 *      [DefaultDispatcher-worker-1 @coroutine#2] - result: 100
 *      [DefaultDispatcher-worker-2 @coroutine#3] - context in launch: [CoroutineId(3), "coroutine#3":StandaloneCoroutine{Active}@..., Dispatchers.Default]
 *
 *      → root 와 coroutineScope 의 CoroutineId 는 모두 (2) 로 동일, launch 만 (3) 으로 새로 부여됨.
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        suspend fun getResult(): Int {
            delay(timeMillis = 100)
            return 100
        }

        val job = CoroutineScope(context = EmptyCoroutineContext).launch {
            log.info { "context in root: $coroutineContext" }

            val result = coroutineScope {
                log.info { "context in coroutineScope: $coroutineContext" }
                getResult()
            }
            log.info { "result: $result" }

            launch {
                log.info { "context in launch: $coroutineContext" }
            }
        }

        job.join()
    }
}
