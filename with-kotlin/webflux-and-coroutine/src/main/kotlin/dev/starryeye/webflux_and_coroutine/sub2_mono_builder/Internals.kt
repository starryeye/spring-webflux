package dev.starryeye.webflux_and_coroutine.sub2_mono_builder

/**
 * sub2 - mono로 반환 (내부 동작 편)
 *
 * 이 파일을 읽기 전에
 *      [Overview] 의 섹션 0 ~ 3 을 먼저 읽고 오는 것을 권장한다.
 *      거기서 정의한 용어(mono { }, ReactorContext, MonoSink) 와
 *      섹션 3 에서 본 "Reactor Context 자동 전달" 동작을 전제로 설명한다.
 *
 * 이 파일에서 다루는 것
 *      "mono { greeting() } 한 줄이 어떻게 진짜 Mono<String> 을 만들어내고
 *       Reactor Context 까지 코루틴에 흘려넣는가" 의 내부 흐름을 따라간다.
 *
 *      section 4. mono { } 가 monoInternal 을 부른다
 *      section 5. monoInternal 이 ReactorContext 를 추출하고 MonoCoroutine 을 시작한다
 *      section 6. MonoCoroutine 이 sink 와 코루틴의 결과/취소를 이어준다
 *      section 7. 한 흐름으로 다시 보기
 *
 *      이하 모든 코드는 다음 위치에 있다.
 *          kotlinx.coroutines.reactor.MonoKt
 *          (artifact: kotlinx-coroutines-reactor)
 *      MonoCoroutine 은 같은 파일 내부의 private class.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 4. mono { } 가 monoInternal 을 부른다
 * ─────────────────────────────────────────────────────────────────────────────
 *      mono { } 의 본체는 단순하다.
 *
 *          public fun <T> mono(
 *              context: CoroutineContext = EmptyCoroutineContext,
 *              block: suspend CoroutineScope.() -> T?
 *          ): Mono<T> {
 *              return monoInternal(GlobalScope, context, block)
 *          }
 *
 *      포인트
 *          - 인자가 두 개뿐: context (선택), 그리고 코루틴 블록.
 *          - GlobalScope 가 내부 scope 로 들어간다.
 *            (GlobalScope = "어떤 부모에도 속하지 않은 최상위 scope".
 *             이렇게 되는 이유는 mono { } 가 만든 코루틴이 자기를 구독하는 시점부터
 *             자기 수명을 갖게 하기 위함이다.)
 *          - 진짜 동작은 monoInternal 이 한다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 5. monoInternal 이 ReactorContext 를 추출하고 MonoCoroutine 을 시작한다
 * ─────────────────────────────────────────────────────────────────────────────
 *      monoInternal 이 핵심이다.
 *
 *          private fun <T> monoInternal(
 *              scope: CoroutineScope, // support for legacy mono in scope
 *              context: CoroutineContext,
 *              block: suspend CoroutineScope.() -> T?
 *          ): Mono<T> = Mono.create { sink ->
 *              val reactorContext = context.extendReactorContext(sink.currentContext())
 *              val newContext = scope.newCoroutineContext(context + reactorContext)
 *              val coroutine = MonoCoroutine(newContext, sink)
 *              sink.onDispose(coroutine)
 *              coroutine.start(CoroutineStart.DEFAULT, coroutine, block)
 *          }
 *
 *      한 줄씩 읽어보기
 *
 *          (1) Mono.create { sink -> ... }
 *              "구독자가 들어와야 비로소 람다 본문이 실행" 되는 형태로 Mono 를 만든다.
 *              sink 는 이 Mono 의 출력 채널이다.
 *                  - sink.success(value) / sink.error(throwable) 로 결과를 흘려보내고
 *                  - sink.currentContext() 로 구독자 쪽 Reactor Context 를 꺼낼 수 있고
 *                  - sink.onDispose(...) 로 구독 취소 시 정리할 자원을 등록할 수 있다.
 *              => 이 시점이 Reactor 측 Context 를 손에 넣을 수 있는 가장 빠른 시점이기도 하다.
 *
 *          (2) sink.currentContext()
 *              구독자 쪽 체인에 .contextWrite { ... } 으로 주입된 값들을 포함한
 *              현재 시점의 Reactor Context 를 돌려준다.
 *              ([Overview] section 3 에서 main 함수의 .contextWrite 으로 넣었던 값이 여기서 보인다.)
 *
 *          (3) context.extendReactorContext(sink.currentContext())
 *              지금 코루틴 컨텍스트의 ReactorContext 항목과
 *              sink 가 알려준 Reactor Context 를 합쳐서
 *              새로운 ReactorContext 어댑터 element 를 만든다.
 *              ("코루틴 컨텍스트 안에 Reactor Context 를 채워 넣는다" 가 한 줄 요약.)
 *
 *          (4) scope.newCoroutineContext(context + reactorContext)
 *              주어진 scope 의 컨텍스트와 합쳐서 최종 코루틴 컨텍스트를 완성한다.
 *              여기에는 Job, Dispatcher, ReactorContext 등이 모두 들어 있다.
 *
 *          (5) MonoCoroutine(newContext, sink)
 *              실제로 사용자 람다(block) 를 돌릴 코루틴 객체를 만든다.
 *              이 객체는 두 개의 정체성을 동시에 가진다:
 *                  - AbstractCoroutine<T> : 코루틴 (suspend 블록을 돌릴 수 있다)
 *                  - Disposable           : Reactor 가 dispose 할 수 있는 자원
 *              덕분에 다음 줄에서 sink.onDispose(coroutine) 이 가능하다.
 *
 *          (6) sink.onDispose(coroutine)
 *              "이 Mono 의 구독이 취소되면 이 코루틴도 dispose 시켜라" 라고 등록.
 *              => 구독자가 끊으면 코루틴이 자동으로 취소되도록 연결하는 부분이다.
 *
 *          (7) coroutine.start(CoroutineStart.DEFAULT, coroutine, block)
 *              마침내 코루틴을 시작한다. block 이 실행되면서 안의 suspend 함수들이 차례로 흘러간다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 6. MonoCoroutine 이 sink 와 코루틴의 결과/취소를 이어준다
 * ─────────────────────────────────────────────────────────────────────────────
 *      MonoCoroutine 은 두 개의 콜백을 오버라이드해서 코루틴 끝과 sink 를 연결한다.
 *
 *          private class MonoCoroutine<in T>(
 *              parentContext: CoroutineContext,
 *              private val sink: MonoSink<T>
 *          ) : AbstractCoroutine<T>(parentContext, false, true), Disposable {
 *
 *              @Volatile
 *              private var disposed = false
 *
 *              override fun onCompleted(value: T) {
 *                  if (value == null) sink.success() else sink.success(value)
 *              }
 *
 *              override fun onCancelled(cause: Throwable, handled: Boolean) {
 *                  val unwrappedCause = unwrap(cause)
 *                  if (getCancellationException() !== unwrappedCause || !disposed) {
 *                      try {
 *                          sink.error(cause)
 *                      } catch (e: Throwable) {
 *                          ...
 *                      }
 *                  }
 *              }
 *          }
 *
 *      한 줄 요약
 *
 *          onCompleted(value)
 *              코루틴 블록이 정상 종료된 시점에 호출된다.
 *              => sink.success(value) 로 흘려보내서 Mono 가 그 값으로 완료된다.
 *              값이 null 이면 빈 Mono 가 되도록 success() 만 호출한다 (Mono 는 0..1 결과니까).
 *
 *          onCancelled(cause)
 *              코루틴이 취소되거나 예외로 끝난 시점에 호출된다.
 *              => sink.error(cause) 로 예외를 Mono 쪽으로 넘긴다.
 *              (단, 외부에서 dispose 호출이 먼저였다면 굳이 error 를 쏘지 않게 disposed 플래그를 본다.)
 *
 *      Disposable
 *          이 클래스가 Disposable 도 구현하기 때문에
 *          section 5 에서 본 sink.onDispose(coroutine) 등록이 가능했다.
 *          dispose 가 호출되면 disposed = true 로 표시되고 코루틴이 취소된다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 7. 한 흐름으로 다시 보기
 * ─────────────────────────────────────────────────────────────────────────────
 *      [구독이 발생한 시점에 무슨 일이 벌어지나]
 *
 *      mono { greeting() }                              // 사용자가 쓰는 코드 한 줄
 *          -> mono(EmptyCoroutineContext, block)        // section 4
 *              -> monoInternal(GlobalScope, ctx, block) // section 5
 *                  Mono.create { sink ->
 *                      reactorContext = ctx.extendReactorContext(sink.currentContext())
 *                          // = .contextWrite 등으로 들어온 Reactor Context 를 코루틴 측 키로 변환
 *                      newContext = scope.newCoroutineContext(ctx + reactorContext)
 *                      coroutine = MonoCoroutine(newContext, sink)
 *                      sink.onDispose(coroutine)
 *                          // 구독 취소 시 코루틴도 같이 취소되도록 연결
 *                      coroutine.start(...)
 *                          // 본격 실행. 안의 suspend 함수들이 흘러간다.
 *                  }
 *
 *      [코루틴 본체가 끝났을 때]                                // section 6
 *          정상 종료    -> MonoCoroutine.onCompleted(value) -> sink.success(value)
 *          취소 / 예외  -> MonoCoroutine.onCancelled(cause) -> sink.error(cause)
 *
 *      이 구조 덕분에 사용자는 "Mono.fromCallable 이 안 된다" 는 제약을 신경 쓸 필요 없이
 *      mono { } 한 줄로 suspend 함수를 Mono 로 노출시키고
 *      Reactor 쪽 Context 까지 자연스럽게 받아 쓸 수 있다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 8. 결론
 * ─────────────────────────────────────────────────────────────────────────────
 *      한 문장으로 줄이면
 *          "mono { } 빌더는 Mono.create 위에서 코루틴을 시작하고,
 *           그 코루틴의 결과 / 예외 / 취소를 sink 로 Mono 에 흘려보낸다.
 *           덤으로 Reactor Context 가 자동으로 코루틴 컨텍스트에 합쳐진다."
 *
 *      대응되는 지점
 *          - "Mono.create 위에서 코루틴 시작"
 *              = monoInternal 의 Mono.create { sink -> ... } 안에서 MonoCoroutine.start 가 호출된다. (section 5)
 *          - "결과 / 예외 / 취소를 sink 로 흘려보냄"
 *              = MonoCoroutine.onCompleted -> sink.success / onCancelled -> sink.error. (section 6)
 *                구독 취소 시에는 sink.onDispose 등록을 통해 코루틴도 같이 취소된다.
 *          - "Reactor Context 자동 합류"
 *              = sink.currentContext() 로 꺼낸 Reactor Context 를 ReactorContext 어댑터로 감싸
 *                코루틴 컨텍스트에 합친다. (section 5 (2)(3))
 *
 *      "같은 메커니즘이 sub1 컨트롤러 케이스에도 들어있다" 와
 *      "future { } / launch { } 등 다른 빌더와의 비교" 는 상위 패키지의 CoroutineUsageGuide 를 참고.
 */
