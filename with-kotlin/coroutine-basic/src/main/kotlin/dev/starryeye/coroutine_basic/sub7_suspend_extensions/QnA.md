# sub7 QnA

## Q1. suspend 확장 함수는 코드를 읽을 때 어떻게 이해하면 되나?

예를 들어 아래 코드를 보자.

```kotlin
val customer = customerService.findCustomerFuture(userId).await()
```

겉으로는 "함수를 호출해서 값을 바로 받는다" 처럼 보이지만, 실제로는 조금 다르다.

1. `findCustomerFuture(userId)` 는 값을 즉시 돌려주지 않는다.
   `CompletableFuture<Customer>` 같은 "나중에 값이 올 비동기 타입" 을 돌려준다.
2. `.await()` 는 그 비동기 타입에 콜백을 연결한다.
   개념적으로는 sub6 에서 직접 썼던 아래 코드와 같다.

```kotlin
future.thenAccept(cont::resume)
```

3. 콜백을 건 뒤 현재 코루틴은 여기서 잠깐 멈춘다.
   이때 멈추는 것은 스레드가 아니라 "이 코루틴의 실행 흐름" 이다.
4. 값이 준비되면 라이브러리가 `cont.resume(value)` 를 호출하고,
   코루틴은 `await()` 다음 줄부터 다시 이어서 실행된다.

즉, `await()` 는 "스레드를 붙잡고 기다리는 함수" 가 아니라
"값이 준비되면 여기부터 다시 실행해달라" 를 등록하는 함수로 보면 된다.

그래서 아래 코드는

```kotlin
val customer = customerService.findCustomerFuture(userId).await()
val products = productService.findAllProductsFlowable(productIds).toList().await()
```

읽을 때 이렇게 생각하면 된다.

- 고객 조회를 요청한다
- 결과가 오면 여기로 돌아와 `customer` 에 넣는다
- 다음으로 상품 조회를 요청한다
- 결과가 오면 여기로 돌아와 `products` 에 넣는다

겉모양은 동기 코드와 비슷하지만, 내부적으로는 "중단 -> 재개" 가 반복된다.

### sub6 와 연결해서 보면

`sub6_build_coroutine_by_hand/OrderAsyncExampleUpgrade2.kt` 에서는
각 비동기 호출마다 `cont::resume` 을 직접 넘기고,
`resumeWith` 안에서 다시 `execute(...)` 로 진입시켰다.

코루틴 + `await()` 는 바로 그 기계를

- 컴파일러가 state machine 으로 만들고
- 라이브러리가 각 비동기 타입별 bridge 를 제공해서

우리가 직접 쓰지 않게 해주는 것이다.

한 줄로 요약하면:

> `await()` 는 "비동기 타입을 값처럼 읽게 해주는 문법" 이고,
> 내부적으로는 "콜백 등록 + 코루틴 재개" 를 숨기고 있다.

## Q2. Reactor 에서는 Publisher 들을 조합했는데, coroutine 에서는 그런 게 필요 없나?

필요 없지는 않다. 다만 조합 방식이 달라진다.

Reactor 에서는 보통 `Mono` / `Flux` 자체를 연산자로 조합한다.

```kotlin
Mono.zip(customerMono, productMono)
    .flatMap { tuple -> createOrder(tuple.t1, tuple.t2) }
```

coroutine 에서는 결과를 먼저 "값" 으로 꺼낸 뒤 일반 코드로 조합하는 일이 많다.

```kotlin
coroutineScope {
    val customerDeferred = async { customerService.findCustomerFuture(userId).await() }
    val productsDeferred = async { productService.findAllProductsFlowable(productIds).toList().await() }

    val customer = customerDeferred.await()
    val products = productsDeferred.await()

    createOrder(customer, products)
}
```

차이는 이렇다.

- Reactor: `Publisher` 를 계속 유지한 채 `map`, `flatMap`, `zip` 으로 조합
- Coroutine: 필요한 시점에 `await()` 해서 값을 꺼내고, `val`, `if`, `for`, `try-catch`, `async` 로 조합

즉, coroutine 이 조합을 없애는 것은 아니다.
단지 "Publisher operator 조합" 이 "일반 코드 조합" 으로 많이 바뀐다.

## Q3. 그럼 언제는 coroutine 이 더 단순하고, 언제는 스트림 조합이 계속 필요한가?

### 1) 단발성 비동기라면 coroutine 쪽이 단순해진다

예:

- 고객 1명 조회
- 주문 1건 생성
- 토큰 1개 조회

이런 작업은 `Mono<T>`, `CompletionStage<T>`, `Uni<T>` 처럼
"결과가 1개" 인 경우가 많다.

이때는 `await()` 후 일반 코드로 이어 쓰는 편이 읽기 쉽다.

### 2) 연속 스트림이라면 조합이 여전히 필요하다

예:

- 실시간 이벤트 스트림
- SSE / WebSocket 메시지
- 계속 흘러오는 로그 / 센서 데이터

이런 경우는 coroutine 에서도 `Flow` 를 써서 조합한다.

```kotlin
flowA.combine(flowB) { a, b -> a to b }
    .filter { (a, b) -> a.isValid && b.isValid }
    .collect { println(it) }
```

즉:

- `Mono` 느낌의 단일 비동기 -> coroutine + `await()` 가 잘 맞는다
- `Flux` 느낌의 연속 스트림 -> `Flow` 나 Reactor 연산자 조합이 계속 중요하다

## Q4. sub5.p4 는 왜 조합 코드가 거의 없어 보이나?

`sub5.p4_coroutine/OrderCoroutineExample.kt` 는
"각 단계의 결과가 다음 단계 입력으로 바로 이어지는 순차 흐름" 이다.

그래서 `await()` 후 값을 변수에 담아 아래로 내려가는 형태만으로도 충분하다.

하지만 독립적인 비동기 작업을 병렬로 돌리고 싶다면 coroutine 에서도 `async` 가 필요하다.
즉,

- 순차 흐름이면 `await()` 만으로도 충분한 경우가 많고
- 병렬 조합이 필요하면 `async/await`
- 스트림 조합이 필요하면 `Flow`

를 사용한다고 보면 된다.

## Q5. Reactor 코드를 읽을 때는 "작업 스레드가 비동기 IO 호출 → event loop 로 반환, 응답 오면 event loop 스레드가 그다음 작업을 이어서 수행" 이라고 머릿속으로 그렸다. coroutine 이 들어오면 어떻게 그리면 되나?

기본 그림은 거의 같다. coroutine 이 새 스레드를 만들지 않는다.
"callback 사슬" 을 "선형 코드처럼 보이는 것" 으로 재배치만 한 것이다.

### Reactor 와 같은 부분

`await` 지점을 풀어보면:

1. 호출 스레드가 `findCustomerFuture(userId).await()` 를 만나면,
   비동기 IO 요청을 던지고 callback (= `cont::resume`) 을 등록한 뒤 그대로 빠져나간다.
2. 호출 스레드는 자유. event loop / pool 로 돌아가 다른 일을 한다.
3. 응답이 오면 비동기 라이브러리가 emit 하는 스레드
   (Reactor 면 Reactor-Netty / Schedulers, JDK Future 면 ForkJoinPool 또는 등록한 Executor) 가
   `cont::resume` 을 호출한다.
4. resume 이 불리면 "await 다음 줄" 부터 이어서 실행된다.

여기까지는 Reactor 의 `flatMap` 안 다음 연산이 emit 스레드 위에서 돌던 것과 동일하다.
sub6 의 `OrderAsyncExampleUpgrade2` 가 손으로 만든 구조가 정확히 이 모양이다.
(`thenAccept(cont::resume)` / `subscribe(cont::resume)` 마다 resume 이 일어나는 스레드가 달라진다.)

### 한 겹 더: Dispatcher

coroutine 에는 한 겹이 추가된다. `CoroutineContext` 안의 `Dispatcher` 다.

- Dispatcher 없는 sub6 수작업 버전 → "resume 스레드 = emit 한 스레드" 가 그대로 노출
- Dispatcher 가 있는 sub5.p4 라이브러리 버전 → emit 스레드가 `cont.resumeWith(...)` 를 호출하긴 하지만,
  실제 "await 다음 줄" 실행은 Dispatcher 가 자기 풀로 옮겨서 돌린다.

비유하면 Reactor 의 `publishOn(Scheduler)` 가 코루틴마다 항상 한 개씩 깔려있다고 보면 된다.

```kotlin
launch(Dispatchers.IO) {
    val customer = customerService.findCustomerFuture(userId).await()
    // 여기는 Dispatchers.IO 풀의 어떤 스레드에서 실행된다.
    // CompletableFuture 가 emit 한 ForkJoinPool 스레드가 아니라.
}
```

### 그래서 머릿속으로 그리면

Reactor 그림에 한 줄만 더하면 된다.

- (Reactor 그림 그대로) IO 호출 → 호출 스레드 즉시 반환 → 응답 시 emit 스레드가 callback 호출
- (추가) 그 callback 이 `cont::resume` 이고,
  **Dispatcher 가 지정돼 있으면 다음 코드를 자기 풀로 한 번 더 디스패치한다.**
  없으면 emit 스레드가 그대로 다음 코드까지 실행.

"스레드를 붙잡지 않는다" 는 핵심 성질은 그대로 유지된다.
WebFlux event loop 친화적인 특성도 그대로다.
바뀐 건 코드 모양 (operator chaining -> 위에서 아래로 선형) 과,
"emit 스레드가 다음 코드를 직접 돌리느냐 / Dispatcher 가 한 번 더 옮기느냐" 의 선택지 한 줄뿐이다.
