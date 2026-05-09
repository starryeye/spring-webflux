package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub1_introduction

/**
 * CoroutineDispatcher
 *
 * 정의
 *      - "Coroutine 이 어느 스레드에서 실행될지 결정" 하는 CoroutineContext.Element 다.
 *      - 좀 더 정확히는, suspend 후 재개될 때 (Continuation.resumeWith)
 *          어느 스레드 / 스레드 풀에서 실행을 이어갈지를 결정한다.
 *      - launch / async 등 builder 가 어떤 스레드에서 시작될지도 이걸로 결정된다.
 *
 *      한 줄로
 *          "어떤 스레드 위에서 돌릴지" 를 정해주는 Element.
 *
 *
 * CoroutineDispatcher
 *      public abstract class CoroutineDispatcher :
 *              AbstractCoroutineContextElement(ContinuationInterceptor),
 *              ContinuationInterceptor {
 *
 *          @ExperimentalStdlibApi
 *          public companion object Key : AbstractCoroutineContextKey<
 *              ContinuationInterceptor,
 *              CoroutineDispatcher,
 *          >(
 *              ContinuationInterceptor,
 *              { it as? CoroutineDispatcher }
 *          )
 *      }
 *
 *      포인트
 *          - AbstractCoroutineContextElement(ContinuationInterceptor) 를 상속한다.
 *              -> Key 가 ContinuationInterceptor 라는 점이 다른 element 와 다르다.
 *                 (CoroutineName, Job 등은 자기 자신을 Key 로 쓰지만
 *                  Dispatcher 는 ContinuationInterceptor 라는 더 일반적인 Key 를 공유한다)
 *          - ContinuationInterceptor 인터페이스도 함께 구현한다.
 *              -> Continuation 을 가로채서 dispatch 할 권한을 가진다는 의미.
 *          - companion object Key 는 "AbstractCoroutineContextKey" 형태로 ContinuationInterceptor 와
 *              CoroutineDispatcher 를 묶는 polymorphic key 다.
 *              덕분에 coroutineContext[CoroutineDispatcher] 로 꺼낼 때
 *              실제로는 ContinuationInterceptor 슬롯에서 꺼낸 뒤
 *              CoroutineDispatcher 인지 캐스팅(it as? CoroutineDispatcher) 하는 식으로 동작한다.
 *
 *
 * 제공되는 Dispatcher 4가지
 *      public actual object Dispatchers {
 *          @JvmStatic public actual val Default:    CoroutineDispatcher = DefaultScheduler
 *          @JvmStatic public actual val Main:       MainCoroutineDispatcher get() = MainDispatcherLoader.dispatcher
 *          @JvmStatic public actual val Unconfined: CoroutineDispatcher = kotlinx.coroutines.Unconfined
 *          @JvmStatic public val           IO:      CoroutineDispatcher = DefaultIoScheduler
 *      }
 *
 *      정리
 *          ┌──────────────┬──────────────────────────────────────────────────────────────┐
 *          │ Default      │ CPU 작업용 공용 풀. 코어 수만큼 worker thread 보유                  │
 *          │              │ ("백그라운드 계산" 의 기본값. 별도 인자 없이 launch 하면 보통 이쪽)     │
 *          ├──────────────┼──────────────────────────────────────────────────────────────┤
 *          │ Main         │ 플랫폼의 "메인 스레드" (Android UI / JavaFX UI / Swing EDT 등)     │
 *          │              │ 사용하려면 별도 의존성 (kotlinx-coroutines-android 등) 이 필요       │
 *          ├──────────────┼──────────────────────────────────────────────────────────────┤
 *          │ Unconfined   │ "특정 스레드에 묶이지 않는다". 호출한 스레드에서 그대로 시작하고          │
 *          │              │ 첫 suspend 이후엔 resume 시켜준 스레드에서 이어진다 (테스트/특수용도)   │
 *          ├──────────────┼──────────────────────────────────────────────────────────────┤
 *          │ IO           │ blocking I/O 용 풀. Default 와 thread 를 공유하지만 더 큰 한도로     │
 *          │              │ 늘어난다 (파일 I/O, JDBC 등 blocking 호출에 사용)                  │
 *          └──────────────┴──────────────────────────────────────────────────────────────┘
 *
 *      주의
 *          - Default 는 DefaultScheduler 라는 내부 구현, IO 는 DefaultIoScheduler 라는 내부 구현이지만
 *              실제로는 같은 worker pool 을 공유한다 (한도와 view 만 다름).
 *          - 별도로 만들고 싶으면 Executors.newFixedThreadPool(...).asCoroutineDispatcher() 같은 식으로
 *              ExecutorService 를 dispatcher 로 감싸 사용할 수 있다 (-> 이후 sub 에서 다룸).
 */
private object CoroutineDispatcherDescription
