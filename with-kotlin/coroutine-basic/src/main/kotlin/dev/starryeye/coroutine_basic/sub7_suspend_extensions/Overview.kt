package dev.starryeye.coroutine_basic.sub7_suspend_extensions

/**
 * sub7 - suspend 확장 함수 정리
 *
 * 이 sub 의 포지션
 *      sub5.p4 에서는 여러 비동기 타입 뒤에 .await() / .awaitSuspending() / .awaitFirst() / .awaitSingle() 를 붙여
 *          코드를 선형으로 만들었다.
 *      sub6 에서는 그 동작이 내부적으로 FSM + CPS + Continuation 으로 어떻게 굴러가는지 손으로 만들어봤다.
 *      sub7 은 그 둘을 연결한다.
 *          "어떤 타입에 어떤 suspend 확장함수를 붙이는지" 를 파일별로 짧게 정리한다.
 *
 * suspend 확장 함수가 하는 일 (공통)
 *      1. 비동기 타입을 값처럼 받는다.
 *      2. 호출 스레드는 block 하지 않고, 현재 코루틴만 suspend 한다.
 *      3. 내부 원리는 "비동기 콜백 -> cont.resume" 이다.
 *          즉 sub6 에서 손으로 만든 구조를 라이브러리가 대신 감싸준 것이다.
 *
 * 파일 구성
 *      future/  : JDK 표준 비동기 (CompletableFuture)
 *      etc/     : 외부 비동기 라이브러리 (RxJava3, Mutiny) + reactive-streams Publisher 추상
 *      reactor/ : Reactor 전용 타입 (Mono, Flux). 스레드 실행 모델까지 자세히 분석.
 *
 *      future/CompletableFutureAwait.kt
 *          CompletionStage / CompletableFuture -> await()
 *
 *      etc/Rxjava3Await.kt
 *          Single / Maybe / Completable / Flowable -> await*, awaitFirst, awaitLast
 *
 *      etc/MutinyAwaitSuspending.kt
 *          Uni -> awaitSuspending(), Multi -> collect().asList().awaitSuspending()
 *
 *      etc/ReactivePublisherAwait.kt
 *          Publisher -> awaitFirst / awaitFirstOrNull / awaitFirstOrDefault / awaitFirstOrElse / awaitLast / awaitSingle
 *
 *      reactor/ReactorMonoAwait.kt
 *          Mono -> awaitSingle(), awaitSingleOrNull()
 *          + Mono.error 처리, 스레드 실행 모델 (subscribeOn 과 Dispatcher 의 분업) 분석
 *
 *      reactor/ReactorFluxAwait.kt
 *          Flux 는 Publisher 라 etc/ReactivePublisherAwait 의 await* 가 그대로 적용된다.
 *          실무 패턴 3가지 (awaitFirst / collectList().awaitSingle / asFlow) 와
 *          asFlow + collect 의 스레드 실행 모델, 취소 전파를 분석.
 *
 * suffix 규칙
 *
 *      await              : 값이 하나 나오는 타입에 주로 사용 (CompletableFuture, Single, Completable 등)
 *      awaitFirst         : 0..N 개 중 첫 값 (비어있으면 예외)
 *      awaitFirstOrNull   : 첫 값 또는 null
 *      awaitFirstOrDefault: 첫 값 또는 default
 *      awaitFirstOrElse   : 첫 값 또는 lazy default
 *      awaitLast          : 끝까지 소비한 뒤 마지막 값
 *      awaitSingle        : 정확히 1개여야 할 때
 *      awaitSingleOrNull  : 0 또는 1개를 허용할 때
 *      awaitSuspending    : Mutiny 쪽 이름만 조금 다를 뿐 의미는 await 와 같다
 *
 *
 * 주의 - 각 예제의 Thread.sleep
 *      학습 편의를 위해 "비동기 소스" 를 흉내낼 때 Thread.sleep 을 썼다.
 *      실제 운영 코드에서는 그 자리에 non-blocking IO 가 온다.
 *      이 sub 의 관심사는 성능 비교보다 "어떤 await* 를 붙여야 하는가" 이다.
 */
