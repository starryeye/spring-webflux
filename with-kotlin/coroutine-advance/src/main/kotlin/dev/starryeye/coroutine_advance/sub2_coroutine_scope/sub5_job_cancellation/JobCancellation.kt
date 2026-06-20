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
 * 먼저 알아둘 것 — "cancel 도 결국 exception 이다"
 *      - coroutine 의 취소는 별도 메커니즘이 아니라, suspend 지점에서 CancellationException 을 throw 하는 방식으로 동작한다.
 *          → 예제 출력의 e2: "StandaloneCoroutine was cancelled" 가 곧 catch (e: Exception) 에 잡힌 그 예외다.
 *      - 그래서 진짜 구분선은 "예외냐 아니냐" 가 아니라 "어떤 예외냐" 이다.
 *
 *          ┌─────────────────────────┬──────────────────────┬───────────┬──────────────┐
 *          │ 던져진 예외               │ 프레임워크의 취급        │ 전파 방향   │ 부모를 죽이나? │
 *          ├─────────────────────────┼──────────────────────┼───────────┼──────────────┤
 *          │ CancellationException    │ "정상 취소" (cancel)    │ 아래로만    │ X (부모가 삼킴) │
 *          │ 그 외 일반 예외           │ "실패" (failure)        │ 위로       │ O (부모를 fail) │
 *          └─────────────────────────┴──────────────────────┴───────────┴──────────────┘
 *
 *      - 둘은 별개가 아니라 "한 메커니즘의 두 단계" 다:
 *          실패(일반 예외) → 위로 전파해 부모를 cancel → 부모는 형제들에게 CancellationException 을 아래로 내려보냄.
 *          즉 "예외의 내려가는 다리" 가 곧 cancellation. (p.191 의 job3 사유가 "Parent job is Cancelling" 인 이유)
 *
 *
 * 그래서 이 패키지는 두 그룹으로 나눈다
 *      sub5_job_cancellation/
 *          cancel/      — CancellationException 으로 동작하는 "정상 취소" (아래로만 전파)
 *              sub1_job_tree/                — Job 트리: 부모-자식 관계의 기초 (p.187)
 *              sub2_cancel_coroutine_scope/  — CoroutineScope.cancel() → 트리 따라 전파 (p.188)
 *              sub3_cancel_root_coroutine/   — root coroutine cancel → 자식들로 전파, cancelAndJoin 으로 확인 (p.189)
 *              sub4_cancel_leaf_coroutine/   — leaf 하나만 cancel → 형제·부모는 안전 (p.190)
 *              sub5_with_timeout_in_tree/    — withTimeout 의 TimeoutCancellationException 도 cancel 성질 → 형제·부모 안전 (p.197)
 *          exception/   — 일반 예외로 인한 "실패" (위로 전파 후 아래로)
 *              sub1_exception_leaf_coroutine/— leaf 의 예외 → 위로 전파되어 부모·scope 까지 cancel (p.191, 전파 4단계 p.192~195)
 *              sub2_supervisor_job/          — launch(SupervisorJob()) 으로 위로 전파를 차단 (p.196)
 *
 *
 * 관련 (이미 다른 곳에서 다룬 주제)
 *      - exception 전파 / CoroutineExceptionHandler  → sub1_coroutine_context/.../sub6_coroutine_exception_handler
 *      - supervisorScope (자식 실패가 형제로 전파되지 않는 변형) → sub4_scoping_function/supervisor_scope
 *      - withTimeout / withTimeoutOrNull 자체 사용법(throw / orNull) → sub4_scoping_function/with_timeout
 *          · TimeoutCancellationException 은 CancellationException → 위 표의 "cancel" 성질(아래로만, 형제·부모 안전).
 *          · 트리 안에서의 전파 동작(p.197)은 cancel/sub5_with_timeout_in_tree 에 둠.
 *
 *
 * 참고
 * — Spring 트랜잭션 rollback 전파와 "모양" 이 닮았다 (직접 관련은 없음) 
 *      - "exception 은 위로 전파, cancel 은 아래로" 라는 규칙이 Spring @Transactional 의 rollback 전파 모델과 큰 그림에서 비슷.
 *      - 단 둘은 독립 라이브러리이고, 코루틴 exception/cancel 이 Spring 트랜잭션에 실제로 쓰이는 건 아니다. 학습용 비유로만.
 */
private object JobCancellationDescription
