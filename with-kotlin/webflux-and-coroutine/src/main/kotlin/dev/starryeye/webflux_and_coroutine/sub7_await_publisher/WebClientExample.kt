package dev.starryeye.webflux_and_coroutine.sub7_await_publisher

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToFlow

/**
 * WebClient 사용 예제 - sub7 의 가장 흔한 케이스.
 *
 * WebClient 의 메서드 체인은 Mono / Flux 를 반환하지만,
 * suspend 함수 안에서는 다음 두 가지 방식으로 자연스럽게 받을 수 있다.
 *
 *      방식 1) 일반 await*: bodyToMono(...).awaitSingle()
 *      방식 2) Spring 이 제공하는 코루틴 확장: awaitBody<T>() / bodyToFlow<T>()
 *              (org.springframework.web.reactive.function.client 패키지)
 *              내부적으로는 bodyToMono / bodyToFlux + await 와 같다 - 짧게 쓰는 syntactic sugar.
 *
 * 실제 외부 호출 없이 보여주기 위해 baseUrl 을 임의 호스트로 둔다.
 * (호출이 실패하더라도 "어떤 코드 모양인지" 가 학습 목적이다)
 */
private val log = LoggerFactory.getLogger("WebClientExample")

class UserApiClient(private val webClient: WebClient) {

    // 방식 1) bodyToMono(...).awaitSingle()
    suspend fun fetchUser(id: Long): String =
        webClient.get()
            .uri("/users/{id}", id)
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle()

    // 방식 2) Spring 코루틴 확장 (.awaitBody<T>())
    suspend fun fetchUserShort(id: Long): String =
        webClient.get()
            .uri("/users/{id}", id)
            .retrieve()
            .awaitBody()                 // = bodyToMono<T>().awaitSingle()

    // Flux -> Flow 로 받아서 코루틴 스트리밍 그대로 처리
    fun streamUsers(): Flow<String> =
        webClient.get()
            .uri("/users")
            .retrieve()
            .bodyToFlux(String::class.java)
            .asFlow()

    // Spring 의 bodyToFlow<T>() 도 동일한 의미
    fun streamUsersShort(): Flow<String> =
        webClient.get()
            .uri("/users")
            .retrieve()
            .bodyToFlow()                // = bodyToFlux<T>().asFlow()
}

/**
 * 동작 시연용 main.
 *      외부 호출 없이 코드 모양만 보여주려고 baseUrl 만 잡아두고 실제 호출은 하지 않는다.
 *      실제 호출까지 해보고 싶다면 baseUrl 을 가용한 서비스로 바꾸고 try/catch 로 감싸 실행하면 된다.
 */
fun main() = runBlocking {
    val webClient = WebClient.create("http://localhost:8080")
    val client = UserApiClient(webClient)

    log.info("UserApiClient created. (실제 호출 없이 시그니처만 시연)")
    log.info("fetchUser(id) 시그니처      : suspend fun fetchUser(id: Long): String")
    log.info("fetchUserShort(id) 시그니처 : suspend fun fetchUserShort(id: Long): String  // .awaitBody()")
    log.info("streamUsers() 시그니처      : fun streamUsers(): Flow<String>                 // .asFlow()")
    log.info("streamUsersShort() 시그니처 : fun streamUsersShort(): Flow<String>            // .bodyToFlow()")

    // (참고) 실제 호출 코드 형태 - 가용한 서비스가 있을 때 주석 풀고 사용:
    //
    //   val user = client.fetchUser(42L)
    //   log.info("user = {}", user)
    //
    //   val all = client.streamUsers().toList()
    //   log.info("all = {}", all)
}
