package dev.starryeye.webflux_and_coroutine.sub3_future_builder

/**
 * sub3 - CompletableFuture로 반환 (내부 동작 편)
 *
 * 이 파일을 읽기 전에
 *      [Overview] 의 섹션 0 ~ 2 를 먼저 읽고 오는 것을 권장한다.
 *      거기서 정의한 용어(future { }, CoroutineScope, Dispatchers.IO) 와
 *      섹션 2 의 사용 패턴을 전제로 설명한다.
 *
 * 이 파일에서 다루는 것
 *      "scope.future { greeting() } 한 줄이 어떻게 진짜 CompletableFuture<String> 을 만들어내고
 *       코루틴의 끝(정상 종료 / 예외 / 취소) 을 그 future 와 연결하는가" 의 내부 흐름을 따라간다.
 *
 *      section 3. CoroutineScope.future 가 CompletableFutureCoroutine 을 만든다
 *      section 4. CompletableFutureCoroutine 이 코루틴 결과/취소를 future 와 이어준다
 *      section 5. 한 흐름으로 다시 보기
 *
 *      이하 모든 코드는 다음 위치에 있다.
 *          kotlinx.coroutines.future.FutureKt
 *          (artifact: kotlinx-coroutines-jdk8)
 *      CompletableFutureCoroutine 은 같은 파일 내부의 private class.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 3. CoroutineScope.future 가 CompletableFutureCoroutine 을 만든다
 * ─────────────────────────────────────────────────────────────────────────────
 *      future { } 빌더의 본체
 *
 *          public fun <T> CoroutineScope.future(
 *              context: CoroutineContext = EmptyCoroutineContext,
 *              start: CoroutineStart = CoroutineStart.DEFAULT,
 *              block: suspend CoroutineScope.() -> T
 *          ): CompletableFuture<T> {
 *              require(!start.isLazy) { "$start start is not supported" }
 *              val newContext = this.newCoroutineContext(context)
 *              val future = CompletableFuture<T>()
 *              val coroutine = CompletableFutureCoroutine(newContext, future)
 *              future.handle(coroutine)
 *              coroutine.start(start, coroutine, block)
 *              return future
 *          }
 *
 *      한 줄씩 읽어보기
 *
 *          (1) require(!start.isLazy)
 *              start 인자로 LAZY 모드는 허용하지 않는다.
 *              CompletableFuture 는 "한번 만들면 곧 결과가 채워질 것" 을 가정하는 API 라서
 *              "구독 시점에 시작" 같은 LAZY 시맨틱이 어울리지 않기 때문이다.
 *
 *          (2) this.newCoroutineContext(context)
 *              호출 시점의 CoroutineScope 컨텍스트와 인자로 받은 context 를 합쳐
 *              최종 코루틴 컨텍스트를 만든다.
 *              [Overview] 예제의 CoroutineScope(Dispatchers.IO).future { ... } 에서는
 *              여기에 Dispatchers.IO 가 들어가 있다.
 *
 *          (3) val future = CompletableFuture<T>()
 *              사용자에게 돌려줄 빈 CompletableFuture 를 미리 만든다.
 *
 *          (4) CompletableFutureCoroutine(newContext, future)
 *              실제로 사용자 람다(block) 를 돌릴 코루틴 객체를 만든다.
 *              이 객체는 두 가지 정체성을 동시에 갖는다.
 *                  - AbstractCoroutine<T>             : 코루틴 (suspend 블록을 돌릴 수 있다)
 *                  - BiFunction<T?, Throwable?, Unit> : CompletableFuture.handle 의 콜백
 *              이 두 번째 정체성 덕분에 다음 줄의 future.handle(coroutine) 이 가능하다.
 *
 *          (5) future.handle(coroutine)
 *              "이 future 가 (정상 / 예외) 어떤 식으로든 완료되면 coroutine.apply 를 불러줘" 라고 등록.
 *              section 4 의 apply 가 cancel() 을 호출하기 때문에
 *              "외부에서 future 를 cancel 하면 코루틴도 취소" 되는 연결이 만들어진다.
 *
 *          (6) coroutine.start(start, coroutine, block)
 *              마침내 코루틴을 시작한다. block 안의 suspend 함수들이 차례로 흘러간다.
 *
 *          (7) return future
 *              사용자에게는 (4) 에서 만든 빈 CompletableFuture 를 돌려준다.
 *              이 future 의 결과는 코루틴 본체가 끝날 때 section 4 에서 채워진다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 4. CompletableFutureCoroutine 이 코루틴 결과/취소를 future 와 이어준다
 * ─────────────────────────────────────────────────────────────────────────────
 *      CompletableFutureCoroutine 은 두 개의 콜백(코루틴 끝 + future 콜백) 을 오버라이드해서
 *      코루틴과 CompletableFuture 사이의 양방향 연결을 만든다.
 *
 *          private class CompletableFutureCoroutine<T>(
 *              context: CoroutineContext,
 *              private val future: CompletableFuture<T>
 *          ) : AbstractCoroutine<T>(context, initParentJob = true, active = true),
 *              BiFunction<T?, Throwable?, Unit> {
 *
 *              override fun apply(value: T?, exception: Throwable?) {
 *                  cancel()
 *              }
 *
 *              override fun onCompleted(value: T) {
 *                  future.complete(value)
 *              }
 *
 *              override fun onCancelled(cause: Throwable, handled: Boolean) {
 *                  future.completeExceptionally(cause)
 *              }
 *          }
 *
 *      한 줄 요약
 *
 *          apply(value, exception)
 *              future 가 외부에서 (cancel / complete) 어떤 식으로든 끝났을 때 호출된다.
 *              (section 3 의 future.handle(coroutine) 으로 등록된 콜백이 이거다.)
 *              본문은 cancel() 한 줄. 즉 "future 가 끝나면 코루틴도 취소" 시킨다.
 *              => future.cancel(...) 같은 외부 취소가 코루틴에 전파되도록 하는 다리.
 *
 *          onCompleted(value)
 *              코루틴 블록이 정상 종료된 시점에 호출된다.
 *              => future.complete(value) 로 흘려보내서 CompletableFuture 가 그 값으로 완료된다.
 *
 *          onCancelled(cause)
 *              코루틴이 취소되거나 예외로 끝난 시점에 호출된다.
 *              => future.completeExceptionally(cause) 로 예외를 future 쪽으로 넘긴다.
 *
 *      양방향 연결의 그림
 *
 *          코루틴 끝 ──── onCompleted / onCancelled ────▶ future.complete / completeExceptionally
 *          코루틴 ◀──── apply (handle 콜백) ──── future 쪽 끝
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 5. 한 흐름으로 다시 보기
 * ─────────────────────────────────────────────────────────────────────────────
 *      [scope.future { greeting() } 한 줄이 호출된 시점]
 *
 *      CoroutineScope(Dispatchers.IO).future { greeting() }
 *          -> CoroutineScope.future(EmptyCoroutineContext, DEFAULT, block)   // section 3
 *              newContext = scope.newCoroutineContext(EmptyCoroutineContext)
 *              future = CompletableFuture<T>()
 *              coroutine = CompletableFutureCoroutine(newContext, future)
 *              future.handle(coroutine)        // future 쪽 끝 -> coroutine.apply -> cancel
 *              coroutine.start(...)            // 본격 실행. 안의 suspend 함수들이 흘러간다.
 *              return future                   // 사용자에게는 이 future 가 즉시 반환됨
 *
 *      [코루틴 본체가 끝났을 때]                                                   // section 4
 *          정상 종료    -> CompletableFutureCoroutine.onCompleted(value) -> future.complete(value)
 *          취소 / 예외  -> CompletableFutureCoroutine.onCancelled(cause) -> future.completeExceptionally(cause)
 *
 *      [외부에서 future 를 취소했을 때]                                            // section 4
 *          future.cancel(...) -> 등록된 handle 콜백 -> CompletableFutureCoroutine.apply -> cancel()
 *          => 코루틴도 같이 취소된다.
 *
 *      이 구조 덕분에 사용자는 "CompletableFuture.supplyAsync 가 안 된다" 는 제약을 신경 쓸 필요 없이
 *      future { } 한 줄로 suspend 함수를 CompletableFuture 로 노출시킬 수 있다.
 *      취소도 양방향으로 자연스럽게 전파된다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 6. 결론
 * ─────────────────────────────────────────────────────────────────────────────
 *      한 문장으로 줄이면
 *          "future { } 빌더는 빈 CompletableFuture 위에서 코루틴을 시작하고,
 *           코루틴의 결과를 future.complete 에, future 쪽 cancel 을 코루틴 취소에 양방향으로 연결한다."
 *
 *      대응되는 지점
 *          - "빈 future 위에서 코루틴 시작"
 *              = future = CompletableFuture<T>() 만들고 CompletableFutureCoroutine.start 호출. (section 3)
 *          - "코루틴 끝 -> future.complete"
 *              = CompletableFutureCoroutine.onCompleted -> future.complete /
 *                onCancelled -> future.completeExceptionally. (section 4)
 *          - "future.cancel -> 코루틴 취소"
 *              = future.handle(coroutine) 등록 + Coroutine.apply 호출 -> coroutine.cancel. (section 4)
 *
 *      구조가 sub2 의 mono { } 와 거의 같다 (sink 자리에 future 가 있을 뿐).
 *      이 둘과 launch { } 까지 한눈에 비교하려면 상위 패키지의 CoroutineUsageGuide 를 참고.
 */
