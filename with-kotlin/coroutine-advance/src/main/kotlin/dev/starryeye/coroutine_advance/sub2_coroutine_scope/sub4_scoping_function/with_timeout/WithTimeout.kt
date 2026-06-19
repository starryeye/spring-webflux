package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.with_timeout

/**
 * Scoping 함수 - withTimeout / withTimeoutOrNull
 *
 *
 * 한 줄로
 *      withTimeout = "block 에 제한시간을 걸고, 그 안에 못 끝내면 block 을 cancel 하는 suspend scoping 함수".
 *          - 시간 초과 시 TimeoutCancellationException 을 throw.
 *          - withTimeoutOrNull 은 같은 동작이지만, 초과 시 throw 대신 null 을 반환.
 *      → 시간이 지나면 run() 이 불려 cancelCoroutine 으로 자기 자신(scope)을 cancel.
 *  *        이때 던지는 TimeoutCancellationException 은 CancellationException 의 하위 타입.
 *
 *
 * 왜 scoping 함수인가
 *      - 내부적으로 TimeoutCoroutine 을 만드는데, 이게 coroutineScope 의 ScopeCoroutine 을 상속한다.
 *          → coroutineScope / supervisorScope 와 같은 "한정된 영역을 묶는 suspend scoping 함수" 계열.
 *      - 다른 점은 그 영역에 "timer" 를 하나 달아두고, 시간이 지나면 그 scope 를 cancel 한다는 것뿐.
 *
 *
 * 핵심
 *      - 취소는 협조적(cooperative)이다.
 *          → block 안에 suspend 지점(delay, 다른 suspend 호출 등)이나 isActive 체크가 있어야 실제로 끊긴다.
 *          → suspend 지점 없는 순수 CPU 루프는 시간이 지나도 중간에 안 끊긴다.
 *      - throw 되는 TimeoutCancellationException 은 CancellationException 이다.
 *          → withTimeout 의 결과가 그 바깥 coroutine 까지 취소시키지는 않는다 (정상적인 cancel 처리로 흡수됨).
 *          → 단, catch (e: Exception) 으로 무심코 삼키면 "취소 신호를 먹어버리는" 함정이 되니 주의.
 *
 *
 * withTimeout vs withTimeoutOrNull
 *      - withTimeout(t) { ... }       : 초과 시 TimeoutCancellationException throw → try-catch 로 처리.
 *      - withTimeoutOrNull(t) { ... } : 초과 시 null 반환 (내부에서 그 예외를 잡아 null 로 바꿔줌) → 분기 처리 깔끔.
 *      - "초과를 예외 흐름으로 다룰지 / 값(null)으로 다룰지" 의 취향·상황 차이.
 *
 *
 * 참고. 취소 중 cleanup 이 필요하면 (NonCancellable)
 *      - 시간 초과로 cancel 된 뒤 finally 등에서 또 suspend 함수를 호출하면, 이미 취소 상태라 바로 다시 cancel 된다.
 *      - 꼭 끝까지 실행해야 하는 정리 작업은 withContext(NonCancellable) { ... } 로 감싸야 한다.
 *      - (이 주제는 cancellation 파트에서 더 자세히 다룬다.)
 *
 *
 * 이어지는 예제
 *      - WithTimeoutExample.kt       - withTimeout 초과 → TimeoutCancellationException 을 try-catch 로 처리
 *      - WithTimeoutOrNullExample.kt - withTimeoutOrNull 초과 → null 반환 / 시간 내 완료 → 값 반환
 */
private object WithTimeoutDescription
