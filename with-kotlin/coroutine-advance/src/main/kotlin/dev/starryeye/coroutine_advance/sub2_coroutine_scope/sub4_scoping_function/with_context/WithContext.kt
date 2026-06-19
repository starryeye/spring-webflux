package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.with_context

/**
 * Scoping 함수 - withContext
 *
 *
 * 한 줄로
 *      withContext = "context 를 잠깐 바꿔서 block 을 실행하고, 결과 T 를 돌려주는 suspend scoping 함수".
 *      대표 사용: dispatcher 만 잠깐 바꿔서 IO / CPU 작업을 격리.
 *
 *
 * coroutineScope 와의 차이
 *      - coroutineScope { }  : context 인자 없음. 외부 scope context 를 그대로 상속하며 Job 만 override (자식 묶음용).
 *      - withContext(ctx)    : context 를 인자로 전달. oldContext + ctx 로 merge 한 newContext 위에서 block 실행.
 *      - 둘 다 suspend scoping 함수, 동기적으로 동작 — block 이 끝날 때까지 caller suspend.
 *      - 둘 다 block 의 마지막 표현 값을 반환 (T).
 *
 *
 * 핵심
 *      - withContext 로 변경된 context 는 withContext 내부에만 영향을 준다.
 *      - block 이 끝나고 나면 caller 는 원래 context (oldContext) 에서 그대로 이어 실행된다.
 *      - 그래서 흔히 "이 부분만 잠깐 IO 풀에서 돌리고 와" 같은 의도로 쓴다.
 *
 *
 * 이어지는 예제
 *      - WithContextExample.kt - launch 안에서 withContext(Dispatchers.IO) 로 잠깐 dispatcher 변경 후 복귀
 */
private object WithContextDescription
