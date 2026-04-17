# Coroutine Builder 비교

코루틴 빌더는 크게 두 그룹으로 나눠서 보면 이해하기 쉽다.

- 새 실행 단위를 시작하고 핸들을 돌려주는 빌더: `launch`, `async`
- 현재 흐름을 block 안으로 들여보내 결과를 받아오는 scope function: `coroutineScope`, `supervisorScope`, `withContext`
- 예외적으로 `runBlocking`은 일반 스레드에서 코루틴 세계로 들어가기 위한 bridge 이며, 호출 스레드를 블로킹한다.

## 그룹 A: 새 실행 단위를 시작하는 빌더

| 빌더 | 호출 위치 | 반환 | 호출자 블로킹? | 핵심 용도 | 예제 |
|---|---|---|---|---|---|
| `runBlocking` | 일반 함수, `main`, 테스트 등 | `T` | 블로킹. 현재 스레드를 멈추고 내부 코루틴 완료를 기다림 | 코루틴 세계로 진입하는 가장 단순한 bridge | `start_coroutine/RunBlockingBuilder.kt` |
| `launch` | `CoroutineScope` 안 | `Job` | 즉시 반환 | 결과값이 필요 없는 비동기 작업 시작 | `start_coroutine/LaunchBuilder.kt` |
| `async` | `CoroutineScope` 안 | `Deferred<T>` | 즉시 반환 | 나중에 `await()`로 받을 결과값이 있는 비동기 작업 시작 | `start_coroutine/AsyncBuilder.kt` |

### 그룹 A 요약

| 구분 | `runBlocking` | `launch` | `async` |
|---|---|---|---|
| 새 실행 단위 시작 | O | O | O |
| 호출자가 받는 핸들 | 없음. block 결과 `T`를 받음 | `Job` | `Deferred<T>` |
| 결과값 | 본문 마지막 값 `T` | 없음 | `Deferred<T>`가 감싸는 `T` |
| 완료 대기 방법 | 호출 스레드가 블로킹됨 | `job.join()` | `deferred.await()` |
| 주 사용 위치 | `main`, 테스트, 샘플 코드 | scope 내부 | scope 내부 |
| 대표 느낌 | "여기서 코루틴 실행 끝날 때까지 막고 기다려" | "작업 하나 던져놓고 필요하면 완료만 기다려" | "작업 하나 던져놓고 나중에 결과를 받아" |

## 그룹 B: 현재 흐름을 block 안으로 들여보내는 scope function

| 빌더 | 호출 위치 | 반환 | 독립 실행 핸들 반환? | 핵심 특징 | 예제 |
|---|---|---|---|---|---|
| `coroutineScope` | `suspend` 함수 안 | `T` | X | 새 scope를 만들고, 내부 자식 코루틴이 모두 끝날 때까지 현재 suspend 흐름을 멈춤 | `scope_function/CoroutineScopeBuilder.kt` |
| `supervisorScope` | `suspend` 함수 안 | `T` | X | `coroutineScope`와 비슷하지만, 자식 실패가 형제 자식에게 자동 전파되지 않음 | `scope_function/SupervisorScopeBuilder.kt` |
| `withContext` | `suspend` 함수 안 | `T` | X | 현재 suspend 흐름에서 dispatcher 같은 `CoroutineContext`를 바꿔 실행 | `scope_function/WithContextBuilder.kt` |

### 그룹 B 요약

| 구분 | `coroutineScope` | `supervisorScope` | `withContext` |
|---|---|---|---|
| 독립 실행 핸들 반환 | X | X | X |
| 새 scope 생성 | O | O | X |
| context 변경 | 기존 context 상속 | 기존 context 상속 | 지정한 context로 변경 |
| 자식 완료 대기 | 모든 자식 완료까지 suspend | 모든 자식 완료까지 suspend | block 완료까지 suspend |
| 자식 실패 전파 | 한 자식 실패가 scope와 형제에게 전파됨 | 한 자식 실패가 형제에게 자동 전파되지 않음 | block 실패가 호출자에게 전파됨 |
| 대표 느낌 | "이 suspend 함수 안에서 구조화된 자식 작업들을 묶어" | "형제 작업은 서로 실패 영향을 덜 받게 묶어" | "이 부분만 다른 dispatcher/context에서 실행해" |

## 가장 중요한 차이

> `launch`/`async`는 새 실행 단위를 만들어서 핸들을 돌려준다.
> `withContext`/`coroutineScope`/`supervisorScope`는 현재 suspend 흐름을 그 블록 안으로 들어가게 했다가 결과를 받아 나온다.
> `즉, 현재 suspend 흐름 안에서 순차적으로 실행되느냐, 아니면 별도의 코루틴 실행 단위로 dispatcher에 스케줄링되어 호출자와 독립적으로 진행될 수 있느냐의 차이이다.`

| 질문 | 새 실행 단위 시작 빌더 | scope function |
|---|---|---|
| 호출자와 따로 진행될 수 있는 작업을 시작하는가? | `launch`/`async`는 O | X |
| 어디서 호출하는가? | `runBlocking`은 일반 함수, `launch`/`async`는 `CoroutineScope` 안 | `suspend` 함수 안 |
| 호출 후 흐름 | `launch`/`async`는 호출자가 다음 줄로 바로 진행 가능 | block 이 끝날 때까지 현재 suspend 흐름이 그 안에 머묾 |
| 반환값의 의미 | 시작한 코루틴의 핸들. `launch`는 `Job`, `async`는 `Deferred<T>` | scope/block 실행 결과 `T` |
| 주된 관심사 | 작업 시작 | 작업 범위, 실패 전파, context 전환 |

`launch`와 `async`는 실제로 새 자식 코루틴을 만든다. 그래서 부모 흐름과 자식 흐름이 동시에 진행될 수 있고, 호출자는 `Job` 또는 `Deferred<T>` 같은 핸들을 받는다.

반면 `coroutineScope`, `supervisorScope`, `withContext`는 그 자체가 호출자와 따로 진행되는 작업을 던지는 도구라기보다 현재 suspend 흐름 안에서 실행 범위나 context를 조정하는 도구다. 호출자는 block 이 끝나야 다음 줄로 진행하고, 반환값도 새 작업 핸들이 아니라 block 의 결과값 `T`다.

주의할 점: `coroutineScope`, `supervisorScope`, `withContext`도 내부적으로 scope, job, continuation 처리가 생긴다. 그래서 "내부 객체가 전혀 생기지 않는다"는 뜻으로 이해하면 안 된다. 여기서 말하는 차이는 **`launch`/`async`처럼 호출자와 병렬로 진행 가능한 작업을 시작하고 `Job`/`Deferred` 핸들을 반환하느냐**다.
