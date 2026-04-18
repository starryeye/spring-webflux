# hello-coroutine

이 프로젝트는 Kotlin Coroutine 의 기본 개념을 작은 예제들로 나누어 설명한다.

핵심 주제는 다음과 같다.

- Kotlin 에서 코루틴이 어떤 라이브러리 성격을 가지는지
- Reactor `Mono` 와 Java `CompletableFuture` 기반 비동기 코드를 코루틴으로 어떻게 더 읽기 쉬운 순차 코드처럼 표현하는지
- `CoroutineContext` 로 코루틴 실행 환경을 어떻게 구성하는지
- `CoroutineScope` 와 structured concurrency 가 부모-자식 작업의 생명주기, 예외, 취소를 어떻게 묶는지
- `runBlocking`, `launch`, `async`, `coroutineScope`, `supervisorScope`, `withContext` 의 역할 차이

## 프로젝트 구성

이 프로젝트는 Spring Boot 4, Kotlin 2, Java 21 기반의 샘플 프로젝트이다.

주요 의존성은 다음과 같다.

- `kotlinx-coroutines-core-jvm`: `suspend`, coroutine builder, dispatcher, scope 등 코루틴 핵심 기능
- `kotlinx-coroutines-reactor`: Reactor `Mono`/`Flux` 와 코루틴 사이의 bridge
- `reactor-core`: `Mono`, `Flux` 기반 비동기 스트림
- `kotlin-logging`: 예제 실행 흐름 확인용 로그

`build.gradle.kts` 에서 `-Dkotlinx.coroutines.debug` JVM 옵션을 설정해 코루틴 디버깅 정보를 로그에서 확인할 수 있게 했다.

## 학습 흐름

### 1. Kotlin 라이브러리 개요

위치: `sub1_library/Library.kt`

Kotlin 이 제공하는 대표 라이브러리를 간단히 소개한다.

- Coroutine: 동시성과 비동기 처리를 지원한다.
- Serialization: 객체 직렬화와 역직렬화를 지원한다.
- Lincheck: 동시성 버그 탐지와 테스트를 지원한다.

이 프로젝트는 그중 Coroutine 에 초점을 둔다.

### 2. Kotlin Coroutine 이 해결하려는 문제

위치: `sub2_kotlin_coroutine/KotlinCoroutine.kt`

코루틴은 비동기 non-blocking 코드를 동기 코드처럼 읽히게 만드는 도구이다.

코루틴의 중요한 축은 두 가지다.

- `CoroutineContext`: dispatcher, 예외 처리, thread local 전달 등 코루틴 실행 환경을 담는다.
- `CoroutineScope`: structured concurrency 와 cancellation 을 제공해 코루틴 생명주기를 구조화한다.

또한 Kotlin Coroutine 은 `Flow`, `Channel` 같은 고급 비동기 모델도 제공한다.

### 3. 기존 비동기 코드와 코루틴 코드 비교

위치:

- `sub3_looks_like_sync_code/LooksLikeSyncCode1.kt`
- `sub3_looks_like_sync_code/LooksLikeSyncCode2.kt`
- `sub3_looks_like_sync_code/repository/Repositories.kt`

예제 repository 는 일부러 서로 다른 비동기 타입을 반환한다.

- `PersonReactorRepository.findPersonByName`: `Mono<Person>` 반환
- `ArticleFutureRepository.findArticleById`: `CompletableFuture<Article>` 반환

코루틴을 사용하지 않는 `LooksLikeSyncCode1.kt` 에서는 `flatMap`, `Mono.fromFuture`, `map`, `subscribe` 로 비동기 흐름을 연결한다. `subscribe` 는 non-blocking 이므로 예제에서는 main thread 종료를 막기 위해 `CountDownLatch` 도 사용한다.

코루틴을 사용하는 `LooksLikeSyncCode2.kt` 에서는 같은 흐름을 아래처럼 순차 코드 형태로 표현한다.

- `Mono<Person>.awaitSingle()` 로 `Person` 을 얻는다.
- `CompletableFuture<Article>.await()` 로 `Article` 을 얻는다.
- 코드는 위에서 아래로 읽히지만 실제 대기는 non-blocking 방식으로 처리된다.

이 부분이 프로젝트의 첫 번째 핵심 메시지다. 코루틴은 비동기 처리를 감추는 것이 아니라, 비동기 작업의 결과 대기를 `suspend` 지점으로 표현해 코드의 형태를 단순하게 만든다.

### 4. CoroutineContext

위치:

- `sub4_coroutine_context/CoroutineContext1.kt`
- `sub4_coroutine_context/CoroutineContext2.kt`

`CoroutineContext1.kt` 는 코루틴 실행 환경을 구성하는 방법을 보여준다.

- `CoroutineName`: 코루틴 이름 지정
- `Dispatchers.IO`: 실행 dispatcher 지정
- `ThreadLocal.asContextElement()`: thread local 값을 코루틴 context 에 실어 thread 가 바뀌어도 값을 이어가게 함

`CoroutineContext2.kt` 는 `CoroutineExceptionHandler` 의 동작 위치를 보여준다.

중요한 점은 `CoroutineExceptionHandler` 는 루트 코루틴에서 의미 있게 동작한다는 것이다. 예제에서는 `runBlocking` 내부에서 `CoroutineScope(Dispatchers.IO).launch(context)` 로 별도의 독립 스코프를 만들기 때문에, `my-coroutine` 은 `runBlocking` 의 자식이 아니라 독립 루트 코루틴처럼 동작한다. 그래서 해당 코루틴에 설정한 exception handler 가 예외를 처리할 수 있다.

### 5. CoroutineScope 와 structured concurrency

위치:

- `sub5_coroutine_scope/StructuredConcurrency1.kt`
- `sub5_coroutine_scope/StructuredConcurrency2.kt`
- `sub5_coroutine_scope/StructuredConcurrency3.kt`

`StructuredConcurrency1.kt` 는 Java `CompletableFuture.runAsync` 를 사용한 비구조적 동시성 예제이다.

비구조적 동시성에서는 부모 작업과 자식 작업의 생명주기가 구조적으로 묶이지 않는다.

- 자식 작업에서 예외가 발생해도 부모가 알기 어렵다.
- 부모 작업이 취소되어도 자식 작업으로 취소가 자동 전파되지 않는다.
- main 이 먼저 끝나면 JVM 종료 때문에 비동기 작업 결과를 보기 어려워 별도의 대기가 필요하다.

`StructuredConcurrency2.kt` 는 `coroutineScope` 로 구조적 동시성을 보여준다.

- scope 안에서 실행한 `launch` 들은 해당 scope 의 자식 코루틴이 된다.
- `coroutineScope` 는 자식 코루틴이 모두 끝날 때까지 종료되지 않는다.
- 따라서 `structured()` 함수가 끝났다는 것은 내부 자식 작업까지 모두 끝났다는 뜻이다.

`StructuredConcurrency3.kt` 는 취소 전파를 보여준다.

- 부모 scope 에서 `cancel()` 하면 자식 코루틴에도 취소가 전파된다.
- `delay` 같은 cancellable suspend 함수는 취소를 감지하고 `CancellationException` 을 발생시킨다.
- 부모와 자식의 취소 흐름이 하나의 구조 안에서 연결된다.

이 부분이 프로젝트의 두 번째 핵심 메시지다. 코루틴은 단순히 비동기 작업을 쉽게 시작하는 도구가 아니라, 작업의 범위와 생명주기를 구조화하는 도구이다.

### 6. Coroutine builder 비교

위치:

- `sub5_coroutine_scope/coroutine_builder/README.md`
- `sub5_coroutine_scope/coroutine_builder/start_coroutine/RunBlockingBuilder.kt`
- `sub5_coroutine_scope/coroutine_builder/start_coroutine/LaunchBuilder.kt`
- `sub5_coroutine_scope/coroutine_builder/start_coroutine/AsyncBuilder.kt`
- `sub5_coroutine_scope/coroutine_builder/scope_function/CoroutineScopeBuilder.kt`
- `sub5_coroutine_scope/coroutine_builder/scope_function/SupervisorScopeBuilder.kt`
- `sub5_coroutine_scope/coroutine_builder/scope_function/WithContextBuilder.kt`

코루틴 빌더는 크게 두 그룹으로 나눠서 이해한다.

첫 번째 그룹은 새 실행 단위를 시작하는 빌더이다.

| 빌더 | 반환 | 핵심 역할 |
|---|---:|---|
| `runBlocking` | `T` | 일반 함수에서 코루틴 세계로 들어가는 bridge. 호출 스레드를 block 한다. |
| `launch` | `Job` | 결과값 없는 새 코루틴을 시작한다. |
| `async` | `Deferred<T>` | 결과값 있는 새 코루틴을 시작하고, 나중에 `await()` 로 결과를 받는다. |

두 번째 그룹은 현재 suspend 흐름을 특정 block 안으로 들여보내는 scope function 이다.

| 함수 | 반환 | 핵심 역할 |
|---|---:|---|
| `coroutineScope` | `T` | 새 scope 를 만들고 내부 자식 코루틴 완료까지 현재 흐름을 suspend 한다. |
| `supervisorScope` | `T` | 자식 실패가 형제 자식에게 자동 전파되지 않는 supervisor scope 를 만든다. |
| `withContext` | `T` | 현재 suspend 흐름의 context 를 바꿔 block 을 실행한다. |

가장 중요한 차이는 다음과 같다.

- `launch` 와 `async` 는 호출자와 별도로 진행될 수 있는 새 코루틴 실행 단위를 만들고 `Job`/`Deferred` 핸들을 반환한다.
- `coroutineScope`, `supervisorScope`, `withContext` 는 새 작업 핸들을 반환하는 도구가 아니라 현재 suspend 흐름 안에서 실행 범위, 실패 전파 방식, context 를 조정하는 도구이다.

자세한 비교표는 `sub5_coroutine_scope/coroutine_builder/README.md` 에 정리되어 있다.

다만 Spring Boot 애플리케이션의 `main` 은 예제별 흐름을 실행하지 않는다. 코루틴 동작을 확인하려면 각 서브패키지의 예제 `main` 을 개별 실행하는 것이 목적에 맞다.

## 전체 요약

이 프로젝트가 설명하는 흐름은 다음 한 문장으로 요약할 수 있다.

Kotlin Coroutine 은 Reactor, CompletableFuture 같은 기존 비동기 API 와 함께 사용할 수 있으며, `suspend` 를 통해 non-blocking 비동기 코드를 순차 코드처럼 읽히게 만들고, `CoroutineContext` 와 `CoroutineScope` 를 통해 실행 환경, 예외 처리, 취소, 부모-자식 작업 생명주기를 구조적으로 관리하게 해준다.

