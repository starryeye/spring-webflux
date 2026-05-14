package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub1_coroutine_scope

/**
 * CoroutineScope() 팩토리 함수
 *
 * 정의 (kotlinx.coroutines)
 *      public fun CoroutineScope(context: CoroutineContext): CoroutineScope =
 *          ContextScope(if (context[Job] != null) context else context + Job())
 *
 *      internal class ContextScope(context: CoroutineContext) : CoroutineScope {
 *          override val coroutineContext: CoroutineContext = context
 *      }
 *
 *      public fun Job(parent: Job? = null): CompletableJob =
 *          JobImpl(parent)
 *
 *
 * 동작 흐름
 *      1) 인자로 받은 context 안에 이미 Job 이 들어있는지 검사한다 (context[Job] != null).
 *      2) 들어있으면 그 context 를 그대로 사용.
 *      3) 없으면 Job() 팩토리로 새 JobImpl 을 만들어 context 에 더해서 채워 넣는다.
 *      4) "Job 이 보장된 context" 를 ContextScope 로 감싸서 CoroutineScope 로 돌려준다.
 *
 * 결론.
 *      CoroutineScope 는 CoroutineScope 함수에 의해 생성될 수 있는데..
 *          CoroutineScope 함수는 CoroutineContext 를 파라미터로 받아서 CoroutineContext 내에 Job 이 있으면 그대로쓰고 없으면 만든다.
 *      (다른 경로 - GlobalScope / MainScope() / coroutineScope { } / 인터페이스 직접 구현 등 - 으로도 CoroutineScope 인스턴스를 얻을 수 있다.)
 *
 * ContextScope 의 정체
 *      - CoroutineScope 의 가장 단순한 구현체.
 *      - 받은 context 를 그대로 coroutineContext 로 노출하는 것 외에 추가 동작이 없다.
 *      - 즉 "CoroutineScope(...) 로 만든 scope" 의 실체는 결국 ContextScope 인스턴스.
 *
 *
 * 참고. 이름이 같은 "CoroutineScope 인터페이스" 와 "CoroutineScope 함수"
 *      두 선언은 별개다. 같은 이름을 쓸 수 있는 건 Kotlin 의 이름공간이 분리돼 있기 때문.
 *          - 타입 위치  (val x: CoroutineScope, class Foo : CoroutineScope) -> 인터페이스로 해석
 *          - 호출 위치  (CoroutineScope(ctx))                                -> 함수로 해석
 *      개념적으로는 짝꿍 — 함수는 인터페이스의 인스턴스 (ContextScope) 를 만들어 돌려주는 게 유일한 목적.
 *
 *
 * 참고. 함수인데 왜 대문자로 시작하는가
 *      Kotlin 공식 코딩 컨벤션의 예외 규칙
 *          "Names of factory functions used to create instances of classes can have the same name as the abstract return type"
 *          (특정 타입의 인스턴스를 만드는 팩토리 함수는 그 반환 타입과 같은 이름을 가질 수 있다.)
 *
 *      표준/코루틴 라이브러리에 같은 패턴이 많다.
 *          val list      = List(10) { it * 2 }
 *          val mutable   = MutableList(10) { 0 }
 *          val job       = Job()
 *          val supervisor= SupervisorJob()
 *          val scope     = CoroutineScope(Dispatchers.IO)
 *      전부 PascalCase 함수이고, 호출부에서는 생성자처럼 자연스럽게 읽힌다.
 *
 *
 * 참고. 왜 굳이 팩토리 함수 형태로 제공하나? 그냥 구현체 생성자를 노출하면 안 되나
 *      CoroutineScope 는 인터페이스라 직접 new 할 수 없다. 인스턴스를 주려면 누군가 구현체를 만들어줘야 하는데
 *          (a) 구현체 (ContextScope) 의 생성자를 public 으로 노출  -> 구현체 타입이 외부 API 가 되어 라이브러리가 내부 구현을 못 바꿈
 *          (b) 팩토리 함수만 public, 구현체는 internal               -> 외부에는 인터페이스만 보이고 구현체는 숨겨짐
 *      코루틴 라이브러리는 (b) 를 택했고, "호출부에서는 생성자처럼 자연스럽게 쓰고 싶다" 는 욕심에 함수 이름을 PascalCase 로 만든 것.
 *      실제로 ContextScope 는 internal class 라서 외부에서 직접 만들 수 없고, 무조건 CoroutineScope(...) 팩토리를 통과해야 한다.
 *      덕분에 위에서 본 "Job 자동 보충" 같은 안전망도 이 한 곳에서 보장된다.
 */
private object CoroutineScopeFunctionDescription
