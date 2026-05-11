package dev.starryeye.coroutine_advance.sub1_coroutine_context.sub3_elements.sub7_coroutine_dispatcher.sub3_io_and_default.io_default_dispatcher_shared

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Default 와 IO 가 같은 worker pool 을 공유한다는 것의 가시화
 *
 * 시나리오
 *      for loop 으로 launch(Default) 와 launch(IO) 를 10000 번 띄우고, 각자 어떤 worker thread 에서 도는지 본다.
 *
 *
 * 관찰 포인트
 *      - launch(Default) 와 launch(IO) 가 동일한 "DefaultDispatcher-worker-N" 위에서 실행된다
 *          -> 같은 worker pool 을 공유한다는 증거.
 *      - worker 번호가 64 를 넘기는 경우가 자주 보인다 (worker-76 등).
 *          -> Default 는 "CPU 개수만큼 고정" 이라고 했지만,
 *             실제로는 IO 의 limitedParallelism (기본 64) 까지 풀이 늘어날 수 있고
 *             그 늘어난 worker 들은 Default 작업도 받아 처리할 수 있다.
 *      - 즉 thread 자체는 IO 가 키우는 만큼 늘어나며, "Default 와 IO 가 의미상 분리된 풀처럼 보일 뿐"
 *          물리적으로는 한 풀이라는 사실을 눈으로 확인.
 *
 * 참고
 *      dispatcher 를 출력하면, Dispatchers.IO, Dispatchers.Default 로 분리되어 보이지만
 *      thread 이름이.. "DefaultDispatcher-worker-..." 로 동일하다..
 *          IO dispatcher 이지만 thread 명이 DefaultDispatcher 라 헷갈릴수 있는점 주의..
 *
 * 출력
 *      ...
 *      22:15 [DefaultDispatcher-worker-30 @coroutine#1019] - thread: DefaultDispatcher-worker-30 ...
 *      22:15 [DefaultDispatcher-worker-34 @coroutine#937]  - thread: DefaultDispatcher-worker-34 ...
 *      22:15 [DefaultDispatcher-worker-76 @coroutine#945]  - thread: DefaultDispatcher-worker-76 ...   <- 64 초과
 *      22:15 [DefaultDispatcher-worker-17 @coroutine#907]  - thread: DefaultDispatcher-worker-17 ...
 *      ...
 */
private val log = KotlinLogging.logger {}

fun main() {
    runBlocking {
        for (i in 1 until 10000) {
            launch(context = Dispatchers.Default) {
                log.info { "[Dispatchers.Default]thread: ${Thread.currentThread().name}" }
            }

            launch(context = Dispatchers.IO) {
                log.info { "[Dispatchers.IO]thread: ${Thread.currentThread().name}" }
            }
        }
    }
}
