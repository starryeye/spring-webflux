package dev.starryeye.webflux_and_coroutine.sub6_lambda_labels

/**
 * sub6 - Kotlin 의 람다 라벨 (@mono / @flux / @launch / ...)
 *
 *          return@mono cached
 *          return@launch
 *          return@flux
 *
 *      이건 mono / flux / launch 빌더가 만든 게 아니라 Kotlin 언어 자체의 "라벨" 문법이다.
 *      어떤 빌더든 람다든 동일한 규칙이라 한 번만 정리하고 잊어버리면 된다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 1. 라벨이 뭔가
 * ─────────────────────────────────────────────────────────────────────────────
 *      "라벨" 은 람다(또는 블록) 에 이름표를 붙이는 문법이다.
 *      그 이름표를 통해 return / break / continue 가 어느 람다를 빠져나갈지 명확히 지정한다.
 *
 *      자동 라벨 (implicit label)
 *          람다가 어떤 함수의 인자로 직접 전달되면, 그 함수 이름이 자동으로 라벨이 된다.
 *
 *              mono { ... }       // 람다는 `mono` 라는 라벨을 자동으로 갖는다
 *              launch { ... }     // 람다는 `launch` 라는 라벨을 자동으로 갖는다
 *              forEach { ... }    // 람다는 `forEach` 라는 라벨을 자동으로 갖는다
 *
 *      명시 라벨 (explicit label)
 *          내가 직접 이름을 붙일 수도 있다 (자동 라벨이 헷갈릴 때 자주 씀).
 *
 *              outer@ for (i in 1..3) {
 *                  for (j in 1..3) {
 *                      if (i * j > 4) break@outer
 *                  }
 *              }
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 2. 왜 필요한가 - lambda 의 return 은 둘러싼 함수에서 return 하려는 의미
 * ─────────────────────────────────────────────────────────────────────────────
 *      Kotlin 에서 람다 안의 일반 `return` 은 람다에서 빠져나가는 게 아니라
 *      "둘러싼 함수" 에서 return 하려는 뜻으로 해석된다 (이걸 non-local return 이라고 부른다).
 *
 *      이게 가능한 람다는 inline 함수에 전달된 람다뿐이다.
 *      mono { } / flux { } / future { } / launch { } / runBlocking { } 은 inline 이 아니므로
 *      그 안에서 그냥 `return value` 라고 쓰면 다음 컴파일 에러가 난다.
 *
 *          'return' is not allowed here
 *
 *      해결책이 라벨이다. "이 람다에서만 빠져나가고 싶다" 라고 명시하면 된다.
 *
 *          return mono {
 *              if (cached != null) {
 *                  return@mono cached    // mono 람다에서만 빠져나간다 -> cached 가 Mono 의 결과
 *              }
 *              loadFromDb()              // 마지막 표현식이 결과
 *          }
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 3. 빌더 별로 어떻게 쓰이나
 * ─────────────────────────────────────────────────────────────────────────────
 *      문법은 모두 동일하다. 자동 라벨 이름만 빌더 함수 이름이 박힌다.
 *
 *          mono { ... return@mono x }                  // x 를 Mono 의 결과로 노출하고 종료
 *          flux { ... return@flux }                    // 더 보낼 게 없을 때 종료 (onComplete 트리거)
 *          future { ... return@future x }              // x 를 CompletableFuture 의 결과로
 *          launch { ... return@launch }                // 코루틴 본체에서 조기 종료
 *          runBlocking { ... return@runBlocking x }    // 블록 결과로 x
 *
 *      forEach 같이 코루틴과 무관한 람다도 똑같다 (Kotlin 일반 문법이라는 증거).
 *
 *          listOf(1, 2, 3).forEach {
 *              if (it == 2) return@forEach  // 다음 요소로 넘어감 (continue 효과)
 *              println(it)
 *          }
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 4. 동작 예제
 * ─────────────────────────────────────────────────────────────────────────────
 *      [LambdaLabelExample] 의 main 을 실행하면
 *      mono / launch / forEach 에서 라벨로 조기 return 하는 모습을 한 화면에서 볼 수 있다.
 *
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * section 5. 결론
 * ─────────────────────────────────────────────────────────────────────────────
 *      한 문장으로 줄이면
 *          "@mono / @launch / @forEach 같은 표시는 Kotlin 의 '람다 라벨' 문법이고
 *           람다 안의 일반 return 이 둘러싼 함수에서 return 하려는 의미라는 기본 규칙 때문에 필요하다."
 *
 *      대응되는 지점
 *          - "둘러싼 함수에서 return 하려는 의미" = section 2 의 non-local return 규칙.
 *          - "그래서 람다에서만 빠져나가려면 라벨이 필요" = return@mono / return@launch / return@forEach.
 *          - "빌더마다 자동 라벨 이름이 함수 이름으로 박힌다" = section 1 (자동 라벨).
 *
 *      이 sub 의 내용은 코루틴이나 Reactor 와는 직접 관계가 없다.
 *      Kotlin 의 일반 문법이라 한 번 익혀두면 모든 람다(빌더 / forEach / let / apply / ...) 에 그대로 적용된다.
 */
