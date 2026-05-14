package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub5_executor_coroutine_dispatcher

/**
 * CoroutineDispatcher - ExecutorCoroutineDispatcher
 *
 * 특징
 *      - 특정 thread 혹은 고정된 개수의 thread pool 을 갖는 Dispatcher 를 만들고 싶을 때 사용한다.
 *      - 내부적으로 n 개 thread 를 가진 java.util.concurrent.Executor 를 만든 뒤
 *          asCoroutineDispatcher() 로 CoroutineDispatcher 로 변환한 형태.
 *      - 내부에 Executor 를 보유하므로 다 쓰고 나면 close() 로 명시적으로 종료해야 한다.
 *          (Closeable 을 구현하고 있다.)
 *
 *
 * 팩토리 함수
 *      newSingleThreadContext(name: String): ExecutorCoroutineDispatcher
 *          -> 단일 thread 짜리 ExecutorCoroutineDispatcher 생성.
 *             내부적으로 newFixedThreadPoolContext(1, name) 을 호출한다.
 *
 *      newFixedThreadPoolContext(nThreads: Int, name: String): ExecutorCoroutineDispatcher
 *          -> nThreads 개의 daemon thread 를 갖는 ScheduledThreadPool 을 만들어 CoroutineDispatcher 로 변환.
 *
 *      두 함수 모두 @DelicateCoroutinesApi 로 표시되어 있다. "Executor 를 매번 새로 만드는" 함수라
 *      반드시 close() 까지 책임지지 않으면 thread leak 이 발생한다는 의미.
 *
 *
 * 내부 구현 (요약)
 *      public actual fun newSingleThreadContext(
 *          name: String
 *      ): ExecutorCoroutineDispatcher = newFixedThreadPoolContext(1, name)
 *
 *      public actual fun newFixedThreadPoolContext(
 *          nThreads: Int, name: String
 *      ): ExecutorCoroutineDispatcher {
 *          val threadNo = AtomicInteger()
 *          val executor = Executors.newScheduledThreadPool(nThreads) { runnable ->
 *              val t = Thread(runnable, if (nThreads == 1) name else name + "-" + threadNo.incrementAndGet())
 *              t.isDaemon = true
 *              t
 *          }
 *          return executor.asCoroutineDispatcher()
 *      }
 *
 *      public abstract class ExecutorCoroutineDispatcher : CoroutineDispatcher(), Closeable {
 *          ...
 *          public abstract val executor: Executor
 *          public abstract override fun close()
 *      }
 *
 *
 * 사용 시 주의
 *      - close() 를 깜빡하면 thread 가 JVM 종료까지 살아있게된다.
 *          (daemon thread 이므로 JVM 종료 자체를 막진 않지만, 살아있는 동안은 자원을 점유한다.)
 *      - lifecycle 을 묶기 좋은 패턴
 *          val dispatcher = newFixedThreadPoolContext(4, "domain")
 *          try { ... } finally { dispatcher.close() }
 *      - 일반적인 경우엔 Dispatchers.IO / Dispatchers.Default 로 충분하다.
 *          이 dispatcher 가 필요한 시점은 "특정 도메인 작업을 격리된 풀에 가두고 싶을 때",
 *          혹은 외부 라이브러리가 특정 thread 위에서만 동작해야 할 때 정도.
 */
private object ExecutorCoroutineDispatcherDescription
