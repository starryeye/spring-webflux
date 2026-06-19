package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.supervisor_scope

/**
 * Scoping 함수 - supervisorScope
 *
 *
 * 한 줄로
 *      supervisorScope = "coroutineScope 와 똑같이 한정된 영역에서 자식 coroutine 들을 묶어 관리하되,
 *                         자식 하나가 실패해도 형제와 scope 자신은 살아남게 하는 suspend scoping 함수".
 *      즉 coroutineScope 의 SupervisorJob 버전.
 *
 *
 * coroutineScope 와의 차이 (핵심)
 *      - coroutineScope { }   : 자식 하나가 예외로 실패하면
 *          → 그 실패가 위(부모 scope)로 전파되어 형제 자식들까지 cancel 되고, scope 자체도 실패한다.
 *      - supervisorScope { }  : 자식 하나가 예외로 실패해도
 *          → 위로 전파되지 않는다. 형제 자식들은 계속 살아서 끝까지 동작하고, scope 자신도 정상 완료된다.
 *          → 취소 전파가 "아래 방향(부모 → 자식)" 으로만 흐르고, "위 방향(자식 → 부모)" 으로는 막힌다.
 *      - 공통점: 둘 다 suspend scoping 함수라 동기적으로 동작 (자식이 모두 끝날 때까지 caller suspend).
 *               둘 다 block 의 마지막 표현 값을 R 로 반환.
 *
 *
 * 그래서 예외는 누가 처리하나
 *      - 자식의 실패가 위로 전파되지 않으므로, supervisorScope 의 직접 자식 launch 는 "root coroutine 처럼" 취급된다.
 *          → 처리되지 않은 예외는 부모로 던져지는 대신 CoroutineExceptionHandler (없으면 기본 핸들러) 로 간다.
 *          → 따라서 launch 자식의 예외는 각 자식 안에서 try-catch 하거나, context 에 CoroutineExceptionHandler 를 심어 처리한다.
 *      - async 자식이라면 예외가 Deferred 에 보관되었다가 await() 하는 쪽으로 던져진다.
 *          → 형제는 영향받지 않으므로, await() 하는 자리에서 try-catch 로 받으면 된다.
 *      - 주의) block "본문에서 직접" 던진 예외 (자식이 아니라 supervisorScope 람다 자체의 throw) 는
 *          여전히 그대로 caller 로 전파된다. supervisor 가 막아주는 것은 어디까지나 "자식의 실패 전파" 다.
 *
 *
 * 언제 쓰나
 *      - "여러 독립적인 작업을 동시에 돌리는데, 그중 하나가 실패해도 나머지는 계속 가야 할 때".
 *          예) 여러 외부 API 를 병렬 호출 → 일부가 실패해도 성공한 것만이라도 모아 쓰고 싶을 때.
 *      - 반대로 "하나라도 실패하면 전부 무의미 (all-or-nothing)" 이면 coroutineScope 가 맞다.
 *
 *
 * 이어지는 예제
 *      - SupervisorScopeExample.kt      - launch 자식 하나가 실패해도 형제가 살아남고, scope 도 정상 완료되는 모습
 *                                         (coroutineScope 와 대비). 예외는 CoroutineExceptionHandler 로.
 *      - SupervisorScopeAsyncExample.kt - async 자식의 예외가 await() 자리로만 던져지고, 형제는 영향받지 않는 모습
 */
private object SupervisorScopeDescription
