package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub3_io_and_default

/**
 * CoroutineDispatcher IO, Default
 *
 * Default
 *      - CPU 코어 수만큼 고정된 크기를 갖는 thread pool 을 제공.
 *
 *      - Dispatcher 가 명시되지 않았을 때 fallback 으로 사용되는 dispatcher.
 *          (부모 scope 의 context 와 launch/async 의 context 인자를 합쳤을 때 ContinuationInterceptor(dispatcher) 가 하나도 없으면 Default 가 붙는다.
 *           - 예: CoroutineScope(EmptyCoroutineContext) 처럼 scope 에 dispatcher 가 없고 launch/async 의 context 인자에도 dispatcher 가 없을 때.
 *                  그때, Default dispatcher 가 사용됨.
 *           반대로 부모 scope 에 dispatcher 가 있고 launch/async 인자에 없으면 부모 것을 그대로 상속한다.
 *           runBlocking 의 자식은 runBlocking 자신의 BlockingEventLoop dispatcher 를 상속한다.)
 *           -> 해당 내용은 dispatcher_inheritance_cases 패키지에서 예시로 다룬다..
 *
 *      - 동시에 실행될 수 있는 task 수가 CPU core 개수와 같다는 뜻이라 -> CPU bound 작업에 적합.
 *
 *      구현
 *          internal object DefaultScheduler : SchedulerCoroutineDispatcher(
 *              CORE_POOL_SIZE,
 *              MAX_POOL_SIZE,
 *              IDLE_WORKER_KEEP_ALIVE_NS,
 *              DEFAULT_SCHEDULER_NAME,
 *          ) { ... }
 *
 *
 * IO
 *      - 기본적으로 최대 64 개까지 늘어나는 가변 크기 thread pool 을 제공 (CPU 코어 수보다 크면 그 수).
 *      - blocking I/O 작업 (파일 I/O, JDBC, blocking HTTP 등) 을 위한 dispatcher.
 *      - 의도: Main / Default 스레드를 막지 않도록 별도 풀에서 I/O blocking 을 격리한다.
 *      - IO bound blocking 에 적합.
 *          Reactor 로 치면, BoundedElastic 이다.
 *
 *      구현
 *          internal object DefaultIoScheduler : ExecutorCoroutineDispatcher(), Executor {
 *              private val default = UnlimitedIoScheduler.limitedParallelism(
 *                  systemProp(
 *                      IO_PARALLELISM_PROPERTY_NAME,
 *                      64.coerceAtLeast(AVAILABLE_PROCESSORS)
 *                  )
 *              )
 *              ...
 *          }
 *
 *
 * 특징
 *      - Default 와 IO 는 사실 같은 worker pool 을 공유한다.
 *          IO 는 그 풀 위에 "최대 동시 실행 수가 더 큰 view (limitedParallelism)" 를 얹어둔 형태.
 *      - 그래서 thread name 도 둘 다 "DefaultDispatcher-worker-N" 으로 같다 (다음 파일 예제 출력에서 확인).
 *      - 다만 한도(parallelism) 가 다르므로
 *          - CPU bound -> Default
 *          - I/O blocking -> IO
 *          이렇게 의도에 맞게 골라 쓰면 된다.
 *      - 해당 내용은 io_default_dispatcher_shared 패키지에서 다룬다.
 */
private object IoDefaultDescription
