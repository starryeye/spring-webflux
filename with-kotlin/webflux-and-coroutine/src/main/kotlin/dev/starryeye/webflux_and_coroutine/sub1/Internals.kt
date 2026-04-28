package dev.starryeye.webflux_and_coroutine.sub1

/**
 * sub1 - Coroutine 사용하기 (내부 동작 편)
 *
 * 이 파일을 읽기 전에
 *      [Overview] 의 섹션 0 ~ 2 를 먼저 읽고 오는 것을 권장한다.
 *      거기서 정의한 용어(suspend, Continuation, Dispatchers.Unconfined, Reactor Context, Mono/Flux 등)와
 *      섹션 2 의 로그 출력(reactor-http-nio-2, MonoCoroutine, Context1, ...)을 전제로 설명한다.
 *
 * 이 파일에서 다루는 것
 *      "@GetMapping suspend fun greet(): String { ... }" 처럼 컨트롤러를 suspend 로 선언했을 때
 *      Spring WebFlux 가 내부에서 어떤 경로로 그 함수를 호출해주는지를 따라간다.
 *
 *      섹션 3. 핸들러 어댑터(RequestMappingHandlerAdapter) 가 컨트롤러 메서드를 부른다
 *      섹션 4. InvocableHandlerMethod 가 suspend 인지 검사한다
 *      섹션 5. CoroutinesUtils.invokeSuspendingFunction 이 코루틴을 Mono 로 감싼다
 *      섹션 6. 한 흐름으로 다시 보기
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 3. 내부 동작 (1) - 핸들러 어댑터가 컨트롤러 메서드를 부른다
 * ─────────────────────────────────────────────────────────────────────────────
 *      WebFlux 의 요청 처리 큰 그림
 *          DispatcherHandler 가 들어온 요청을 보고 어떤 컨트롤러 메서드가 처리할지 매핑한 다음,
 *          그 메서드를 실제로 "호출" 해주는 단계는 HandlerAdapter 라는 컴포넌트가 담당한다.
 *          @Controller / @RestController 기반 컨트롤러를 호출해주는 구현체가 다음이다.
 *
 *              org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter
 *              (artifact: spring-webflux)
 *
 *          public class RequestMappingHandlerAdapter
 *                  implements HandlerAdapter, ApplicationContextAware, InitializingBean {
 *
 *              @Override
 *              public Mono<HandlerResult> handle(ServerWebExchange exchange, Object handler) {
 *                  ...
 *                  HandlerMethod handlerMethod = (HandlerMethod) handler;
 *                  InvocableHandlerMethod invocableMethod =
 *                          this.methodResolver.getRequestMappingMethod(handlerMethod);
 *                  ...
 *                  return this.modelInitializer
 *                          .initModel(handlerMethod, bindingContext, exchange)
 *                          .then(Mono.defer(() -> invocableMethod.invoke(exchange, bindingContext)));
 *              }
 *          }
 *
 *      이 코드에 등장하는 객체들
 *
 *          HandlerMethod
 *              "어떤 빈의 어떤 메서드를 부를지" 를 들고 있는 정보 객체. (호출은 안 한다)
 *
 *          InvocableHandlerMethod
 *              HandlerMethod 를 감싸서 실제로 invoke 까지 해주는 객체.
 *              위치는 다음 섹션에 적는다.
 *
 *      흐름 정리
 *          1. 라우팅 결과로 받은 handler 객체를 HandlerMethod 로 캐스팅
 *          2. 그로부터 실제 호출 객체인 InvocableHandlerMethod 를 얻음
 *          3. 모델(@ModelAttribute 등)을 초기화한 뒤 invocableMethod.invoke(...) 를 호출
 *
 *      이 단계까지는 일반 컨트롤러도 똑같이 거친다.
 *      suspend 인지 아닌지의 분기는 다음 단계인 invoke 안에 있다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 4. 내부 동작 (2) - InvocableHandlerMethod 가 suspend 인지 검사한다
 * ─────────────────────────────────────────────────────────────────────────────
 *      위치
 *          org.springframework.web.reactive.result.method.InvocableHandlerMethod
 *          (artifact: spring-webflux)
 *
 *      invoke 안에서 컨트롤러 메서드가 suspend 함수인지 확인하고 호출 방법을 분기한다.
 *
 *          public Mono<HandlerResult> invoke(
 *                  ServerWebExchange exchange,
 *                  BindingContext bindingContext, Object... providedArgs) {
 *
 *              return getMethodArgumentValues(exchange, bindingContext, providedArgs)
 *                  .flatMap(args -> {
 *                      Object value;
 *                      Method method = getBridgedMethod();
 *                      boolean isSuspendingFunction = KotlinDetector.isSuspendingFunction(method);
 *                      try {
 *                          if (isSuspendingFunction) {
 *                              value = CoroutinesUtils.invokeSuspendingFunction(method, getBean(), args);
 *                          } else {
 *                              value = method.invoke(getBean(), args);
 *                          }
 *                      }
 *                      ...
 *                  });
 *          }
 *
 *      두 개의 등장인물
 *
 *          KotlinDetector.isSuspendingFunction(method)
 *              위치: org.springframework.core.KotlinDetector (artifact: spring-core)
 *              주어진 자바 Method 가 코틀린 suspend 함수인지 판별한다.
 *              ([Overview] 섹션 0 에서 본 것처럼 코틀린 컴파일러가 suspend 함수의 마지막 파라미터로
 *               Continuation 을 끼워 넣기 때문에, 그 시그니처 패턴 + Kotlin 메타데이터를 같이 보고 판단한다.)
 *
 *          CoroutinesUtils.invokeSuspendingFunction(method, target, args)
 *              위치: org.springframework.core.CoroutinesUtils (artifact: spring-core)
 *              suspend 함수를 실제로 호출해서 결과를 reactive Publisher 로 변환해주는 유틸.
 *              다음 섹션에서 자세히 본다.
 *
 *      정리
 *          - suspend 가 맞다 -> CoroutinesUtils.invokeSuspendingFunction 으로 호출
 *          - 일반 함수 -> 그냥 reflection 호출 (method.invoke)
 *
 *      여기가 바로 "WebFlux 컨트롤러를 suspend 로 선언해도 그냥 동작하는" 마법의 분기점이다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 5. 내부 동작 (3) - invokeSuspendingFunction 이 코루틴을 Mono 로 감싼다
 * ─────────────────────────────────────────────────────────────────────────────
 *      위치
 *          org.springframework.core.CoroutinesUtils#invokeSuspendingFunction
 *          (artifact: spring-core)
 *
 *      원본은 자바로 되어있는데 Kotlin reflection 과 메타데이터(KFunction 등) 가
 *      섞여있어 그대로 보면 길다. 같은 로직을 코틀린 스타일로 옮기면 의도가 또렷해진다.
 *
 *          // (개념을 보기 위한 Kotlin 풀이판이다. 실제 원본은 자바다.)
 *          fun invokeSuspendingFunction(
 *              method: Method, target: Any, vararg args: Any
 *          ): Publisher<*>? {
 *              val function = Objects.requireNonNull(method.kotlinFunction)!!
 *              if (method.isAccessible && !function.isAccessible) {
 *                  function.isAccessible = true
 *              }
 *              val classifier = function.returnType.classifier
 *              val mono = mono(Dispatchers.Unconfined) {
 *                  function.callSuspend(
 *                      getSuspendedFunctionArgs(target, *args)
 *                  )
 *              }.filter { result ->
 *                  result != Unit
 *              }.onErrorMap(InvocationTargetException::class.java) { obj ->
 *                  obj.targetException
 *              }
 *
 *              return if (classifier != null && classifier is Flow<*>) {
 *                  mono.flatMapMany { (it as Flow<Any>).asFlux() }
 *              } else mono
 *          }
 *
 *      한 줄씩 읽어보기
 *
 *          (1) mono(Dispatchers.Unconfined) { ... }
 *              위치: kotlinx.coroutines.reactor.MonoKt#mono
 *              (artifact: kotlinx-coroutines-reactor)
 *
 *              "코루틴을 시작해서 그 결과를 Mono 로 노출" 시키는 빌더.
 *              여기서 Dispatchers.Unconfined 가 들어가기 때문에
 *              새 스레드로 점프하지 않고 호출 스레드(=Reactor 이벤트 루프) 위에서 진행된다.
 *              [Overview] 섹션 2 의 로그에서 봤던 "thread: reactor-http-nio-2" 가 여기서 결정되는 것.
 *
 *          (2) function.callSuspend(args)
 *              Kotlin reflection 으로 suspend 함수를 호출.
 *              내부에서 Continuation 을 자동으로 만들어 넘겨준다.
 *              ([Overview] 섹션 0 에서 말했던 "마지막 파라미터에 끼워 넣는 그 Continuation" 이 여기서 들어간다.)
 *
 *          (3) .filter { it != Unit }
 *              suspend fun handler() 처럼 반환 타입이 Unit 인 경우
 *              Mono 를 비어있는(empty) 상태로 만들어서 응답 본문이 없게 처리한다.
 *
 *          (4) .onErrorMap(InvocationTargetException) { it.targetException }
 *              reflection 으로 호출하면 사용자 예외가 InvocationTargetException 으로 한 번 감싸진다.
 *              감싸진 채로 흘려보내면 @ExceptionHandler 등이 진짜 예외 타입을 못 잡으므로
 *              여기서 다시 풀어준다.
 *
 *          (5) classifier is Flow<*> 분기
 *              컨트롤러 반환 타입이 Flow<T> 면 (예: SSE / 스트리밍 응답)
 *                  -> mono.flatMapMany { (it as Flow<Any>).asFlux() } 로 풀어서 Flux 처럼 다룬다.
 *              아니면
 *                  -> 단일 결과의 Mono 를 그대로 반환한다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 6. 한 흐름으로 다시 보기
 * ─────────────────────────────────────────────────────────────────────────────
 *      [요청 도착]
 *          -> RequestMappingHandlerAdapter.handle(exchange, handler)        // 섹션 3
 *          -> InvocableHandlerMethod.invoke(...)                            // 섹션 4
 *              -> KotlinDetector.isSuspendingFunction(method) ?
 *                  YES -> CoroutinesUtils.invokeSuspendingFunction(...)     // 섹션 5
 *                              -> mono(Dispatchers.Unconfined) { method.callSuspend(...) }
 *                              -> Flow 면 flatMapMany(asFlux), 아니면 그대로 Mono
 *                  NO  -> method.invoke(...)
 *          -> 결과 Mono / Flux 를 응답 본문으로 직렬화해서 반환
 *
 *      이 구조 덕분에 컨트롤러를 suspend 로 선언하기만 하면
 *      별도의 빌더(mono { }, runBlocking 등) 없이 자연스럽게
 *      Reactor 파이프라인 위에서 코루틴이 실행된다.
 */
