package dev.starryeye.webflux_and_coroutine.sub5_real_world_examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * sub2 (Mono 반환) 패턴이 가장 자주 나오는 자리 - WebFilter.
 *
 * org.springframework.web.server.WebFilter#filter 가 Mono<Void> 를 강제한다.
 * 우리는 그 시그니처를 만족하면서 본문은 코루틴으로 짜고 싶으니 mono { } 로 감싼다.
 *
 * @Component 라서 빈으로 등록되며, sub1 의 /greet 같은 엔드포인트를 호출하면
 * 이 필터의 [in] / [out] 로그가 같이 찍힌다.
 */
@Component
class LoggingWebFilter : WebFilter {

    private val log = LoggerFactory.getLogger(LoggingWebFilter::class.java)

    private suspend fun preProcess(exchange: ServerWebExchange) {
        delay(1) // 사전 비동기 작업 흉내 (예: 트레이스 ID 발급, 토큰 사전 검증)
        log.info("[in]  {}", exchange.request.path)
    }

    private suspend fun postProcess(exchange: ServerWebExchange) {
        delay(1) // 사후 비동기 작업 흉내 (예: 메트릭 발사)
        log.info("[out] {} -> {}", exchange.request.path, exchange.response.statusCode)
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return mono {
            preProcess(exchange)
            chain.filter(exchange).awaitSingleOrNull()
            postProcess(exchange)
        }.then()
    }
}
