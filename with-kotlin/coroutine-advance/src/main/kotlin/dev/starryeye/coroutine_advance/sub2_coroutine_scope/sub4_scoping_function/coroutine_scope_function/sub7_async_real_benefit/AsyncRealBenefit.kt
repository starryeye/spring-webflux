package dev.starryeye.coroutine_advance.sub2_coroutine_scope.sub4_scoping_function.coroutine_scope_function.sub7_async_real_benefit

/**
 * async + coroutineScope 의 진짜 이점
 *
 *
 * 일단 의문부터
 *      sub6 의 해결 코드를 다시 보자.
 *
 *          val result = coroutineScope {
 *              val d1 = async { ... }
 *              val d2 = async { ... }
 *              val d3 = async { ... }
 *              d1.await() + d2.await() + d3.await()    // ← 결국 await 셋 다 부른다
 *          }
 *
 *      "감싸기만 했지 await 셋 다 부르는 건 똑같은데 뭐가 좋아진 거지?" 라는 의문이 자연스럽다.
 *      실제로 위 코드만 보면 시각적인 이득은 거의 없다.
 *      이점은 "보이지 않는 곳" 에 있다. 아래 세 가지.
 *
 *
 * 이점 1) suspend 함수 안에서 async 를 쓰려면 coroutineScope 가 사실상 필수다
 *
 *      async 는 CoroutineScope.async 확장 함수다.
 *      즉, 호출자에게 "CoroutineScope" 라는 게 있어야만 부를 수 있다.
 *
 *          ❌ 컴파일 안 됨 — suspend fun 본문엔 CoroutineScope receiver 가 없다.
 *
 *              suspend fun fetchAll(): Int {
 *                  val d1 = async { fetchA() }       // 'async' 호출 불가
 *                  val d2 = async { fetchB() }
 *                  return d1.await() + d2.await()
 *              }
 *
 *          ✅ coroutineScope 가 CoroutineScope receiver 를 빌려준다 → 호출 가능.
 *
 *              suspend fun fetchAll(): Int = coroutineScope {
 *                  val d1 = async { fetchA() }
 *                  val d2 = async { fetchB() }
 *                  d1.await() + d2.await()
 *              }
 *
 *      실전에서 일반 suspend fun 으로 추출할 때는 coroutineScope 가 사실상 강제된다.
 *
 *      → 이게 가장 자주 부딪치는 진짜 이점. 자세한 모양은 SuspendFunctionAsyncExample.kt 참고.
 *
 *
 * 이점 2) 한 묶음 안에서만 예외가 격리된다 (실패해도 다른 작업에 안 번진다)
 *
 *      coroutineScope 로 감싸지 않으면 async 들은 outer (launch / runBlocking ...) 의 자식이 된다.
 *      한 명이 죽으면 outer 자체가 cancel 되어 outer 의 다른 자식까지 통째로 죽는다.
 *
 *          launch {
 *              val d1 = async { ... }
 *              val d2 = async { error("BOOM") }     // 실패!
 *              val d3 = async { ... }
 *              val unrelated = launch { ... }       // 무관한 작업
 *
 *              // d2 의 예외 → outer launch 의 Job 이 cancel → d1, d3, unrelated 까지 함께 cancel
 *          }
 *
 *      coroutineScope 로 감싸면 async 들이 ScopeCoroutine 의 자식이 된다.
 *      한 명이 실패하면 ScopeCoroutine 안의 형제만 정리되고, ScopeCoroutine 이 예외를 caller 로 throw.
 *      바깥의 무관한 작업은 영향 없음.
 *
 *          launch {
 *              try {
 *                  val result = coroutineScope {
 *                      val d1 = async { ... }
 *                      val d2 = async { error("BOOM") }
 *                      val d3 = async { ... }
 *                      d1.await() + d2.await() + d3.await()
 *                  }                                           // ← 여기서 예외 throw
 *              } catch (e: Throwable) {
 *                  // 이 묶음만 잡아낸다
 *              }
 *              val unrelated = launch { ... }                  // 위 묶음 실패와 무관하게 진행 가능
 *          }
 *
 *      직관적으로 정리하면: "이 세 async 는 한 단위" 라는 격리 박스를 만들어 주는 게 coroutineScope.
 *
 *
 * 이점 3) 자식이 떠다니지 않게 lifecycle 을 보장한다
 *
 *      coroutineScope 없이 async 를 받아두면, 코드 흐름이 중간에 빠져나갈 때 이미 시작된 async 들이 그대로 떠다닐 수 있다.
 *
 *          val d1 = async { ... }
 *          val d2 = async { ... }
 *          val d3 = async { ... }
 *          if (someCondition) return              // ← return 되더라도.. d1, d2, d3 가 살아서 자원 잡고 계속 돌아간다
 *          val result = d1.await() + d2.await() + d3.await()
 *
 *      coroutineScope 안에 두면 블록을 벗어나는 어떤 경로 (정상 / 예외 / cancel) 로든
 *      ScopeCoroutine 의 lifecycle 이 블록과 함께 끝난다. → 자식들도 자동으로 정리.
 *
 *          val result = coroutineScope {
 *              val d1 = async { ... }
 *              val d2 = async { ... }
 *              val d3 = async { ... }
 *              if (someCondition) return@coroutineScope 0      // ← 이 경우에는 d1, d2, d3 가 깔끔히 cancel
 *              d1.await() + d2.await() + d3.await()
 *          }
 *
 *
 * 한 줄로
 *      "await 를 똑같이 호출하는데 이게 뭐가 좋아?" 라는 의문은 trivial 예제에선 자연스럽다.
 *      이점은 trivial 예제에선 안 보이고 아래 세 군데에서 비로소 드러난다.
 *          (a) suspend fun 으로 추출할 때 (제일 큰 이유)
 *          (b) 한 묶음의 예외 격리
 *          (c) lifecycle leak 방지
 *
 *      launch 쪽 coroutineScope 이점이 "보이는 이득" (.join() 이 사라짐) 이라면,
 *      async 쪽은 "보이지 않는 안전성" 이라고 기억하면 된다.
 *
 */
private object AsyncRealBenefitDescription
