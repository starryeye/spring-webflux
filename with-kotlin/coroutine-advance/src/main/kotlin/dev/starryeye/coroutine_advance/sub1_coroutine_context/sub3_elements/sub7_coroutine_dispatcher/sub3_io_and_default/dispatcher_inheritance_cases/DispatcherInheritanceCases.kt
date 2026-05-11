package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub3_io_and_default.dispatcher_inheritance_cases

/**
 * launch / async 의 dispatcher 결정 규칙 4가지 케이스
 *
 * 핵심
 *      newCoroutineContext(context) 가 실제 사용할 dispatcher 를 결정한다.
 *
 *          public actual fun CoroutineScope.newCoroutineContext(context: CoroutineContext): CoroutineContext {
 *              val combined = foldCopies(coroutineContext, context, true)   // (1) 부모 scope + 인자 합치기 (인자 우선)
 *              ...
 *              return if (combined !== Dispatchers.Default && combined[ContinuationInterceptor] == null)
 *                  debug + Dispatchers.Default else debug                    // (2) 둘 다 dispatcher 없으면 Default fallback
 *          }
 *
 *      즉,
 *          1) 부모 scope 의 context 와 launch/async 의 context 인자를 합친다 (인자가 우선).
 *          2) 합친 결과에 ContinuationInterceptor(=dispatcher) 가 하나도 없으면 그제서야 Default 가 fallback 으로 붙는다.
 *
 *
 * 4가지 케이스 정리
 *
 *      | case | 부모 dispatcher  | launch 인자 dispatcher | 실제 사용              | 예제 파일                       |
 *      |------|------------------|------------------------|------------------------|----------------------------------|
 *      | 1    | 있음 (IO)        | 없음                   | IO  (부모 상속)        | Case1ScopeHasArgumentNone.kt     |
 *      | 2    | 있음 (IO)        | 있음 (Default)         | Default (인자 우선)    | Case2ScopeHasArgumentHas.kt      |
 *      | 3    | 없음             | 없음                   | Default (fallback)     | Case3ScopeNoneArgumentNone.kt    |
 *      | 4    | 없음             | 있음 (IO)              | IO  (인자)             | Case4ScopeNoneArgumentHas.kt     |
 *
 *
 * "부모" 의 의미와 예제 구조
 *      여기서 "부모" 는 두 가지 의미를 동시에 갖는다.
 *          (a) Job 계층 (구조적 동시성): 부모의 Job 이 자식 Job 의 parent 가 된다.
 *          (b) Context 상속: 부모의 coroutineContext 가 자식이 상속할 base context 가 된다.
 *
 *      이 두 의미를 만족하는 "부모" 는 (i) 실제 실행 중인 코루틴이거나, (ii) Job 과 context 를 들고 있는 CoroutineScope 다.
 *      그래서 예제 4개를 두 가지 형태로 나눠서 보여준다:
 *
 *          case 1, 2 (부모에 dispatcher 가 있다):
 *              runBlocking(Dispatchers.IO) { launch(...) { } } 로 표현.
 *              runBlocking 자신이 IO 위에서 실행되는 부모 coroutine 이고, 그 안의 launch 가 자식.
 *              부모-자식 관계가 lexical nesting 으로 보인다.
 *
 *          case 3, 4 (부모에 dispatcher 가 없다):
 *              runBlocking 은 항상 자기 dispatcher (BlockingEventLoop 또는 인자로 받은 것) 를 갖기 때문에
 *              "dispatcher 없는 부모" 를 만들 수 없다.
 *              그래서 CoroutineScope(CoroutineName("parent")) 를 별도로 만들고 parentScope.launch { } 형태로 보여준다.
 *              이때 바깥 runBlocking { ... } 은 부모-자식 관계와 무관하고, parentScope.launch.join() 을 호출하기 위해
 *              두른 컨텍스트일 뿐이다. 실제 부모는 parentScope.
 *
 * 참고
 *      - IO 와 Default 는 같은 worker pool 을 공유해서 thread name 이 "DefaultDispatcher-worker-N" 으로 같게 나오므로,
 *        구분을 위해 dispatcher 자체를 ContinuationInterceptor key 로 꺼내 함께 출력한다.
 */
private object DispatcherInheritanceCasesDescription
