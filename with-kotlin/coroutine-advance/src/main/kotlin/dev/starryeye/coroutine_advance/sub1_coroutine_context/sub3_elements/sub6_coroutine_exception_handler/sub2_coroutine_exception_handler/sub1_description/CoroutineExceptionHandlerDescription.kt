package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub6_coroutine_exception_handler.sub2_coroutine_exception_handler.sub1_description

/**
 * CoroutineExceptionHandler
 *
 * 정의
 *      - CoroutineContext.Element 의 한 종류.
 *          앞에서 본 launch 의 "잡히지 않는 exception" 을 받아서 처리할 마지막 라인의 hook.
 *      - context 에 얹어두면, 자식 launch 에서 발생한 exception 이 부모로 cancellation 되면서
 *          최종적으로 root coroutine 에서 이 handler 의 handleException(...) 으로 전달된다.
 *
 *
 * CoroutineExceptionHandler
 *      public interface CoroutineExceptionHandler : CoroutineContext.Element {
 *          public companion object Key : CoroutineContext.Key<CoroutineExceptionHandler>
 *
 *          public fun handleException(
 *              context: CoroutineContext,
 *              exception: Throwable,
 *          )
 *      }
 *
 *      포인트
 *          - CoroutineName, Job 처럼 companion object Key 패턴.
 *          - 보통은 CoroutineExceptionHandler { ctx, e -> ... } DSL 함수로 만든다.
 *
 *
 * 주의사항
 *      - "root coroutine" 의 context 에 있어야 한다.
 *          여기서 root 는 "별도 CoroutineScope 의 직속 launch/async" 또는 GlobalScope.launch 등을 말한다.
 *      - 중간(중첩된) launch 의 context 에 붙이면 무시된다 (MiddleLaunch 예제 참고).
 *          -> 자식의 exception 은 부모로 그냥 전파될 뿐, 중간 자식의 handler 는 사용되지 않기 때문.
 *
 *
 * launch 에는 적용, async 에는 적용 불가
 *      - launch 의 exception 은 보관되지 않고 전파만 되므로 -> 마지막에 root 의 handler 가 받아 처리한다.
 *      - async 의 exception 은 Deferred 안에 보관되었다가 await() 시점에 caller 로 던져진다.
 *          -> caller 의 try-catch 가 처리해야 할 책임이고, handler 는 무시된다 (AsyncExceptionHandler 예제 참고).
 *
 *
 * 정리
 *      - launch 트리의 root 에 CoroutineExceptionHandler 를 붙이면 그 트리에서 발생하는 exception 이 그쪽으로 모인다.
 *      - 중간 launch 에 붙이거나 async 에 붙이면 의미 없음 -> 일반 try-catch + await 로 해결해야 함.
 */
private object CoroutineExceptionHandlerDescription
