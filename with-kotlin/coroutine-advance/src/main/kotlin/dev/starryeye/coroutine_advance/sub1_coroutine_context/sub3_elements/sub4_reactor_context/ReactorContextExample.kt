package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub4_reactor_context

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono
import reactor.util.context.Context

/**
 * CoroutineContext 의 종류 (4) - ReactorContext 예제
 *
 * 시나리오
 *      Mono { } 를 만들고, 마지막에 .contextWrite { it.put("who", "starry") } 로 reactor Context 에
 *      "who" -> "starry" 를 주입한 뒤 subscribe 한다.
 *      그러면 mono { } 람다 안에서 그 reactor Context 가 ReactorContext element 로 자동 주입되어 보인다.
 *
 * 예제
 *      1) mono { } 안에서 launch(Dispatchers.IO) 를 실행해도, 부모(coroutine) 의 ReactorContext 가 그대로 전파됨
 *      2) 그 안에서 ReactorContext 를 꺼내, reactor.Context 의 .get<String>("who") 로 값을 읽을 수 있음
 *         -> 처음 subscribe 의 contextWrite 가 넣은 "starry" 가 보인다
 *      3) 새로운 reactor.Context 를 만들어 ReactorContext(newContext) 형태로 launch 에 전달하면
 *         -> 그 launch 안의 ReactorContext 는 새 값("eye") 으로 덮어씌워진다
 *      4) launch 안에서 새로 Mono.create { } 를 만들고 .contextWrite(...) 로 reactor 쪽에 흘려보내면
 *         -> Mono 안에서 it.contextView().getOrDefault("who", "world") 로 그 값("eye")을 다시 읽을 수 있다
 *
 * 출력
 *      [DefaultDispatcher-worker-2 @coroutine#2] - hello1, starry   <- 외부에서 contextWrite 로 넣은 값
 *      [DefaultDispatcher-worker-2 @coroutine#3] - hello2, eye      <- launch 에 새로 주입한 값
 */
private val log = KotlinLogging.logger {}

fun main() {

    // mono { } - "coroutine 으로 작성한 람다" 를 Mono<T> 로 변환해주는 reactor coroutine builder.
    //      람다 안의 coroutine context 에는 subscribe 시점의 reactor Context 가
    //      ReactorContext element 로 자동 주입된다.
    val greeting: Mono<String> = mono {
        // 진입 시점의 reactor Context (= 외부에서 .contextWrite { } 로 넣은 값)
        val initialReactorContext: Context? = this.coroutineContext[ReactorContext]?.context

        // (1) mono { } 안에서 또 launch -> 부모 coroutine 의 context 를 그대로 상속
        launch(context = Dispatchers.IO) {
            // (2) coroutineContext[ReactorContext]?.context  ==  외부 Mono 가 contextWrite 로 넣어준 reactor Context
            val context: Context? = this.coroutineContext[ReactorContext]?.context
            val who = context?.get<String>("who") ?: "world"
            log.info { "hello1, $who" }   // -> "hello, starry"
        }

        // (3) 새 reactor.Context 를 만들어 ReactorContext 로 감싸 launch 에 직접 주입.
        //      "who" 를 "eye" 로 override 한다.
        val newContext: Context = (initialReactorContext ?: Context.empty()).put("who", "eye")
        launch(context = ReactorContext(newContext)) {
            val context: Context? = this.coroutineContext[ReactorContext]?.context

            // (4) 다시 Mono 를 만들어 reactor 쪽으로 흘려보낸다.
            //      Mono 안에서는 it.contextView() 로 reactor Context 를 읽는다.
            //      .contextWrite(context) 로 위 newContext 를 그대로 reactor 체인에도 넣어준다.
            Mono.create<String> {
                val who = it.contextView().getOrDefault("who", "world")
                it.success("hello2, $who")
            }
                .contextWrite(context ?: Context.empty())
                .subscribe { log.info { it } }   // -> "hello, eye"
        }

        // mono<String> 의 결과값 (이 예제에서는 사용하지 않지만 타입 추론을 위해 String 을 반환)
        "done"
    }

    // 마지막으로 외부 (Reactor 측) 에서 .contextWrite 로 "who" -> "starry" 를 주입한 뒤 subscribe.
    //      이 값이 mono { } 안의 coroutine 으로 ReactorContext 를 통해 전달된다.
    greeting
        .contextWrite { it.put("who", "starry") }
        .subscribe()

    // mono { } 는 비동기로 동작하므로, main 이 너무 빨리 끝나지 않게 잠깐 대기.
    Thread.sleep(500)
}
