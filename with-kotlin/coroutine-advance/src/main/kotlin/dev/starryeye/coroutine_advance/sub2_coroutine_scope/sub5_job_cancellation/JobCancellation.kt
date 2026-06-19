package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub5_job_cancellation

/**
 * Job cancellation (카테고리 소개)
 *
 *
 * 목표
 *      - structured concurrency 의 "취소" 측면을 다룬다.
 *      - coroutine 들은 Job 을 매개로 부모-자식 트리를 이루고, 이 트리를 따라 취소가 전파된다.
 *      - "누가 누구의 부모인가(Job 트리)" 를 먼저 보고, "취소가 그 트리를 따라 어떻게 흐르는가" 로 이어진다.
 *
 *
 * 핵심 두 가지
 *      1) Job 트리
 *          - CoroutineScope 에 coroutine 이 추가되면, 그 coroutine 의 부모는 CoroutineScope 의 Job.
 *          - coroutine 안에서 또 launch 하면, 그 coroutine 자신이 부모가 되어 부모-자식 구조가 쌓인다.
 *      2) 취소 전파
 *          - 부모(또는 scope)가 cancel 되면 그 아래 자식들로 취소가 전파된다 (아래 방향).
 *          - 자식의 delay 같은 suspend 지점에서 CancellationException 이 throw 되며 협조적으로 끊긴다.
 *
 *
 * 이 패키지 구조
 *      sub5_job_cancellation/
 *          sub1_job_tree/                — Job 트리: CoroutineScope-coroutine, coroutine-coroutine 부모/자식 관계 (p.187)
 *          sub2_cancel_coroutine_scope/  — CoroutineScope.cancel() 시 취소가 트리를 따라 전파되는 모습 (p.188)
 *
 *
 * 관련 (이미 다른 곳에서 다룬 주제)
 *      - exception 전파 / CoroutineExceptionHandler  → sub1_coroutine_context/.../sub6_coroutine_exception_handler
 *      - supervisorScope (자식 실패가 형제로 전파되지 않는 변형) → sub4_scoping_function/supervisor_scope
 *      - withTimeout / withTimeoutOrNull (제한시간 초과 시 cancel) → sub4_scoping_function/with_timeout
 */
private object JobCancellationDescription
