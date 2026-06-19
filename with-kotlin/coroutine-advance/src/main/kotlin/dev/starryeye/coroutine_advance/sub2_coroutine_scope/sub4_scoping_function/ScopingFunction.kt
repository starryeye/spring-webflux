package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function

/**
 * Scoping 함수 (카테고리 소개)
 *
 *
 * 무엇인가
 *      - "주어진 block 동안 새 CoroutineScope 를 만들고, block 이 끝날 때까지 그 안의 자식 coroutine 들을 묶어 관리하는" suspend 함수.
 *      - 즉 "한정된 코드 블록 단위로 structured concurrency 를 형성" 하기 위한 도구.
 *
 *
 * 코루틴 builder 와의 결정적 차이
 *      - builder (launch / async / runBlocking) 는 새 Coroutine 을 만들어 비동기로 떨어져 나가는 함수.
 *      - scoping 함수는 새 Coroutine 을 만들지만 동기적으로 동작 — block 안 모든 자식이 끝날 때까지 caller 가 suspend.
 *
 *
 * 대표 scoping 함수들
 *      - coroutineScope { ... }      — 일반적인 묶음 scope
 *      - supervisorScope { ... }     — 자식 하나가 실패해도 형제들이 살아남도록 SupervisorJob 을 쓰는 변형.
 *      - withContext(context) { ... } — context 를 잠깐 바꿔서 그 위에서 실행하는 변형 (가장 자주 보임).
 *
 *
 * 이 패키지 구조
 *      sub4_scoping_function/
 *          coroutine_scope_function/        — coroutineScope 함수
 *          with_context/           — withContext 함수
 *          supervisor_scope/       — supervisorScope 함수 (자식 실패가 형제로 전파되지 않는 변형)
 */
private object ScopingFunctionDescription
