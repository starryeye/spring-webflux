package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub4_reactor_context

/**
 * CoroutineContext 의 종류 (4) - ReactorContext
 *
 * 배경
 *      - Reactor 의 Mono / Flux 는 자체적인 "Context" (= ContextView 의 mutable 버전) 를 들고 다닌다.
 *          -> 요청 단위의 attribute 를 비동기 체인 끝까지 전달하기 위한 통로.
 *      - Coroutine 은 CoroutineContext 라는 자체 통로를 따로 가진다.
 *      - 두 세계 (Reactor <-> Coroutine) 를 오갈 때 이 둘을 서로 어떻게 연결할 것인가? 가 문제이다.
 *
 *
 * ReactorContext 가 하는 일
 *      Reactor 의 Context 를 한 겹 감싸서 CoroutineContext.Element 로 만들어 둔 어댑터.
 *      이게 coroutine context 안에 들어 있으면
 *          - mono { } 등 reactor builder 가 coroutine 을 시작할 때 이걸 꺼내 Reactor Context 로 다시 넘겨줄 수 있고
 *          - coroutine 안에서도 coroutineContext[ReactorContext] 로 꺼내 Reactor 쪽 값을 읽을 수 있다.
 *
 *
 * ReactorContext
 *      public class ReactorContext(public val context: Context) :
 *          AbstractCoroutineContextElement(ReactorContext) {
 *
 *          // Context.of 는 인자가 이미 Context 면 zero-cost (그대로 사용)
 *          public constructor(contextView: ContextView) : this(Context.of(contextView))
 *
 *          public companion object Key : CoroutineContext.Key<ReactorContext>
 *
 *          override fun toString(): String = context.toString()
 *      }
 *
 *      포인트
 *          - AbstractCoroutineContextElement(ReactorContext) 를 상속 -> Element 구현체.
 *              생성자에 자기 자신의 Key (companion object) 를 넘겨주는 패턴 (CoroutineName 과 동일).
 *          - 안에 보관하는 건 "Reactor 의 Context" 그 자체.
 *          - companion object Key : CoroutineContext.Key<ReactorContext>
 *              -> coroutineContext[ReactorContext] 로 꺼낼 때 이 Key 를 사용.
 *          - ContextView 를 받는 보조 생성자도 제공 -> Reactor 측에서 받은 read-only view 를 그대로 감싸기 편함.
 *
 *
 * 어디서 자동으로 끼어드나
 *      - mono { ... } / flux { ... } 같은 reactor coroutine builder 안에서 시작된 coroutine 의 context 에는
 *          상위 Mono/Flux 의 Context 가 ReactorContext 로 자동 주입된다.
 *      - 즉 "subscriber 가 contextWrite 로 넣은 값" 이 mono { } 람다 안에서 coroutine 으로 그대로 보인다.
 *      - 반대로 coroutine 쪽에서 ReactorContext 를 context 에 얹어두면, 그 안에서 사용된 Mono 체인의
 *          하위 연산자들도 그 Context 를 본다 (다음 파일 ReactorContextExample 참고).
 *
 *
 * 사용 흐름 (다음 파일 ReactorContextExample 에서 코드로 확인)
 *      - Mono { ... }.contextWrite { it.put(...) }              // Reactor 측에서 Context 주입
 *      - 그 Mono 를 mono { } / awaitSingle() 로 받으면 coroutine 의 coroutineContext[ReactorContext] 로 보임
 *      - launch(ReactorContext(myContext)) { ... } 로 직접 주입도 가능
 *      - 그 launch 안에서 새 Mono 를 만들면 그 ReactorContext 가 다시 Reactor 쪽으로 흘러들어간다
 *
 *
 * 정리
 *      ReactorContext = "Reactor 의 Context 를 CoroutineContext 의 Element 로 감싼 어댑터".
 *      Reactor 와 Coroutine 사이에서 요청-스코프 attribute 를 양방향으로 흘려보내기 위한 다리 역할.
 */
private object ReactorContextDescription
