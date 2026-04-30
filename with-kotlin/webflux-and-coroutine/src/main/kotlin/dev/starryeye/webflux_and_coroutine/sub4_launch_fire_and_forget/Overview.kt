package dev.starryeye.webflux_and_coroutine.sub4_launch_fire_and_forget

/**
 * sub4 - Unit으로 반환 (fire-and-forget)
 *
 * 이 sub 의 한 줄 요약
 *      "반환 타입이 Unit 인 인터페이스 본문에서 suspend 함수를 호출해야 한다면
 *       launch { } 빌더로 코루틴 하나를 띄우고 잊어버리는(fire-and-forget) 패턴을 쓸 수 있다."
 *
 *      단, 이 패턴은 PDF 강의자료가 명시적으로 "권장하지는 않음" 이라고 단 케이스다.
 *      쓰기 전에 section 3 의 주의사항을 먼저 보길 권한다.
 *
 *      구성
 *          Overview.kt              - 0. 용어, 1. 문제, 2. 해결, 3. 주의사항(권장하지 않는 이유)
 *          GreetBlockingService.kt  - "변경 불가능하다고 가정하는" Unit 반환 외부 인터페이스
 *          GreetBlockingServiceImpl.kt - launch { } 빌더 사용 + main
 *
 *      sub2 / sub3 / sub4 비교
 *          sub2: override fun findGreet(): Mono<String>             = mono { greeting() }
 *          sub3: override fun findGreet(): CompletableFuture<String> = scope.future { greeting() }
 *          sub4: override fun findGreet()                            = scope.launch { ... }   // 결과 없음, fire-and-forget
 *
 *          전체 비교 그림은 상위 패키지의 CoroutineUsageGuide 참고.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * 0. 시작하기 전에 - 용어 정리
 * ─────────────────────────────────────────────────────────────────────────────
 *      이 sub 에서 새로 등장하거나 강조되는 용어만 정리한다.
 *      Mono / suspend / Dispatcher 같은 기본 용어는 sub1, sub2 의 Overview 섹션 0 참고.
 *
 *      launch { } 빌더
 *          위치: kotlinx.coroutines.Builders#launch (정확히는 BuildersKt.launch)
 *          (artifact: kotlinx-coroutines-core)
 *
 *          public fun CoroutineScope.launch(
 *              context: CoroutineContext = EmptyCoroutineContext,
 *              start: CoroutineStart = CoroutineStart.DEFAULT,
 *              block: suspend CoroutineScope.() -> Unit
 *          ): Job
 *
 *          - mono { } / future { } 처럼 "코루틴을 시작" 하는 빌더지만 결과를 외부로 노출하지 않는다.
 *          - 반환은 Job 한 개뿐. Job 은 "그 코루틴의 수명 핸들" 이지 결과값이 아니다.
 *          - sub3 의 future { } 처럼 CoroutineScope 의 확장 함수이므로 scope 가 필요하다.
 *
 *      Job
 *          코루틴의 "수명/상태" 를 나타내는 핸들.
 *          .cancel(), .join() (suspend), .isActive 등을 호출할 수 있다.
 *          launch { } 의 반환값이지만 Unit 반환 인터페이스 안에서는 보통 그냥 버려진다.
 *          "버려진다" 는 점이 sub4 패턴의 본질적 약점 (section 3 참고).
 *
 *      Dispatchers.IO
 *          IO bound 작업을 염두에 둔 dispatcher.
 *          내부적으로 Dispatchers.Default 와 같은 워커 풀을 공유하므로
 *          실제 실행 스레드 이름은 "DefaultDispatcher-worker-*" 로 찍힌다.
 *          (sub3 와 동일)
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 1. 문제 - Unit 반환 함수 안에서 suspend 함수를 호출할 수 없다
 * ─────────────────────────────────────────────────────────────────────────────
 *      배경 (sub2/sub3 와 같은 구조다. 반환 타입이 Unit 으로 바뀌었을 뿐)
 *
 *          interface GreetBlockingService {
 *              fun findGreet()      // Unit 반환
 *          }
 *
 *          구현 안에서 부르고 싶은 함수는 suspend 다.
 *
 *              private suspend fun greeting(): String {
 *                  delay(100)
 *                  return "hello"
 *              }
 *
 *      잘못된 시도
 *
 *          override fun findGreet() {
 *              log.info(greeting())  // 컴파일 에러
 *          }
 *
 *          오류 메시지
 *              "Suspend function 'greeting' should be called only from a coroutine
 *               or another suspend function"
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 2. 해결 - launch { } 빌더로 코루틴을 띄운다
 * ─────────────────────────────────────────────────────────────────────────────
 *      Unit 반환 인터페이스의 경우 가장 단순한 해결책은 launch { } 다.
 *
 *          override fun findGreet() {
 *              CoroutineScope(Dispatchers.IO).launch {
 *                  log.info(greeting())  // OK - launch 람다 안은 코루틴 빌더 람다
 *              }
 *          }
 *
 *      왜 OK 인가
 *          launch 의 람다 시그니처는 "suspend CoroutineScope.() -> Unit" 이다.
 *          그 자체가 suspend 함수이므로 안에서 다른 suspend 함수를 호출해도 된다.
 *
 *      sub2/sub3 와의 차이
 *          - mono { }    : 결과 -> Mono<T> 에 흘려보냄
 *          - future { }  : 결과 -> CompletableFuture<T> 에 흘려보냄
 *          - launch { }  : 결과 -> "버려짐". 외부에서는 결과를 받을 수 없다.
 *
 *      실행 환경
 *          [GreetBlockingServiceImpl] 의 main 을 실행해보면 다음과 비슷한 로그가 찍힌다.
 *
 *              02:44 [DefaultDispatcher-worker-1 @coroutine#1] - hello
 *
 *          main 의 끝에 Thread.sleep(1000) 이 있는 이유 ───
 *              findGreet() 는 코루틴을 띄우기만 하고 즉시 반환된다 (fire-and-forget).
 *              만약 sleep 없이 main 이 그대로 끝나면 JVM 이 종료되어
 *              아직 일하고 있던 코루틴은 결과를 출력하기도 전에 잘려나간다.
 *              "끝을 기다릴 수 없다" 는 sub4 의 가장 큰 약점이 이 한 줄에 드러난다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 3. 주의사항 - 왜 "권장하지 않는가"
 * ─────────────────────────────────────────────────────────────────────────────
 *      PDF 강의자료의 표현
 *          "권장하지는 않지만 정말 불가피하게 Unit 을 반환해야 한다면.."
 *          "반환값이 사용되지 않고"
 *          "언제 해당 job 이 완료될지 알지 않아도 되고"
 *          "중간에 cancel 을 할 필요가 없는 경우"
 *
 *      이 네 줄을 풀면 다음 약점들이 된다.
 *
 *      (1) 호출자가 끝을 기다릴 수 없다
 *          findGreet() 는 코루틴을 띄우자마자 즉시 반환된다.
 *          호출자는 작업이 끝났는지, 결과가 무엇이었는지를 알 수 있는 손잡이가 없다.
 *          (launch 가 Job 을 반환하긴 하지만 Unit 반환 인터페이스 시그니처에서는 그걸 외부에 못 노출한다.)
 *
 *      (2) 예외가 호출자에게 전파되지 않는다
 *          코루틴 안에서 발생한 예외는 호출자 스레드에 던져지지 않는다.
 *          잡지 않으면 CoroutineExceptionHandler 또는 부모 scope 의 정책에 따라 처리되는데,
 *          매번 새 CoroutineScope(Dispatchers.IO) 를 만드는 위 예제 같은 코드에서는
 *          예외가 조용히 사라지거나 잘못된 곳에서 터질 수 있다.
 *
 *      (3) 매번 새 CoroutineScope 를 만드는 패턴 자체가 위험하다
 *          위 예제는 호출 한 번에 CoroutineScope 한 개를 새로 만든다.
 *          이 scope 는 어떤 부모에도 묶이지 않으므로
 *          - 외부에서 cancel 할 방법이 없고 (구조적 동시성 깨짐)
 *          - 누수가 잘 보이지 않는다.
 *          실무에서는 적어도 액티비티/요청/애플리케이션 단위의 명시적인 scope 에 띄워야 한다.
 *
 *      (4) Reactor / CompletableFuture 와 달리 결과 합성이 안 된다
 *          mono { } / future { } 는 다른 reactive / async 체인과 합쳐 쓸 수 있지만
 *          launch { } 는 "그냥 작업 하나" 라서 응답 합성에 끼울 수 없다.
 *
 *      이 패턴이 그래도 괜찮은 경우
 *          - 진짜 부수효과만 발생시키는 작업 (감사 로그 / 메트릭 / 캐시 워밍 등)
 *          - 호출자가 결과/완료/취소에 관심이 전혀 없는 작업
 *          - 빠뜨려도 큰 문제가 없는 작업 (재시도/멱등 처리가 다른 곳에 있는 작업)
 *
 *      가능하면 우선 고려할 대안
 *          - 인터페이스를 suspend fun findGreet() 로 바꿀 수 있는지 검토
 *          - 결과 / 완료 신호가 필요하다면 sub2 (Mono) 또는 sub3 (CompletableFuture) 패턴으로
 *          - 진짜 fire-and-forget 이 정답이라면, 적어도 매 호출마다 새 scope 를 만들지 말고
 *            상위 컴포넌트가 들고 있는 명시적인 scope 에 launch 하기
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 4. 결론
 * ─────────────────────────────────────────────────────────────────────────────
 *      한 문장으로 줄이면
 *          "launch { } 는 결과를 외부에 노출할 채널이 없는 빌더라
 *           코루틴을 띄우고 즉시 잊어버린다.
 *           호출자가 결과 / 완료 / 예외 / 취소에 전혀 관심 없는 부수 효과 작업에서만 합리적이다."
 *
 *      대응되는 지점
 *          - "결과 노출 채널이 없다"
 *              = mono { } / future { } 는 결과를 Mono / CompletableFuture 에 흘려보내지만
 *                launch { } 는 Job 만 반환한다. 그 Job 도 Unit 반환 인터페이스에서는 보통 버려진다. (section 2)
 *          - "호출자가 끝 / 예외에 관심 없는 자리에만"
 *              = 결과 / 완료 / 예외가 호출자에게 자동 전파되지 않는다. (section 3 (1)(2))
 *          - "그래도 쓸 거면"
 *              = 매 호출마다 새 CoroutineScope 를 만들지 말고 컴포넌트 단위 scope 를 재사용한다. (section 3 (3))
 *
 *      "결과를 받아야 한다면 sub2 / sub3 패턴으로 고치는 게 맞다" 와
 *      다른 빌더와의 비교는 상위 패키지의 CoroutineUsageGuide 를 참고.
 */
