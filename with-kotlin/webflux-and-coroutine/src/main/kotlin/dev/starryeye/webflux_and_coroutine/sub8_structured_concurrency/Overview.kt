package dev.starryeye.webflux_and_coroutine.sub8_structured_concurrency

/**
 * sub8 - 여러 suspend 결합 (structured concurrency)
 *
 * 한 줄 요약
 *      여러 suspend 작업을 동시에 띄우거나, 다른 dispatcher 로 잠깐 옮겨 실행하고 싶을 때
 *      쓰는 도구는 세 가지 - coroutineScope { } / async + await / withContext.
 *
 * 핵심 도구
 *      coroutineScope { ... }
 *          여러 자식 코루틴을 묶어 한 단위로 다루는 "범위" 를 만든다.
 *          모든 자식이 끝나야 빠져나오고, 한 자식이 실패하면 형제도 같이 취소된다 (구조적 동시성).
 *
 *      async { ... }.await()
 *          코루틴을 띄우고 결과(Deferred<T>) 를 받는다.
 *          await() 로 그 결과를 가져온다.
 *          coroutineScope 안에서 두 개를 동시에 띄우면 두 작업이 병렬로 실행된다.
 *
 *      withContext(dispatcher) { ... }
 *          블록을 실행할 dispatcher 를 잠깐 바꾼다.
 *          예: 블로킹 IO 가 어쩔 수 없이 필요할 때 withContext(Dispatchers.IO) 로 감싸기.
 *
 * 예제 파일
 *      ParallelFetchExample.kt - async/await 로 두 작업을 동시에 실행
 *      WithContextExample.kt   - withContext 로 dispatcher 전환 (블로킹 호출 격리)
 *
 * 결론
 *      한 문장으로 줄이면
 *          "여러 작업을 동시에 = async + await, 잠깐 다른 스레드 풀로 = withContext, 그것들의 묶음 = coroutineScope."
 */
