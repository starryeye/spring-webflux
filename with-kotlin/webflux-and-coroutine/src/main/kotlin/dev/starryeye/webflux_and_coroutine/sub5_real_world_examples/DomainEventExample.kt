package dev.starryeye.webflux_and_coroutine.sub5_real_world_examples

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * sub4 (Unit, fire-and-forget) 가 자주 나오는 자리 - 감사 로그 / 메트릭 같은 부수 효과.
 *
 * 호출자는 작업 결과 / 완료 / 예외에 관심이 없고, "그냥 시작해" 라는 의미다.
 * 단, sub4 의 Overview section 3 에서 본 약점 중 "매번 새 scope 만들기" 만큼은 아래 예제처럼
 * 컴포넌트 단위 scope (+ SupervisorJob) 를 재사용해 보완하는 게 좋다.
 */
interface AuditLogger {
    fun log(event: String)
}

class CoroutineAuditLogger : AuditLogger {

    private val log = LoggerFactory.getLogger(CoroutineAuditLogger::class.java)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private suspend fun sendToAuditService(event: String) {
        delay(50) // 외부 audit 서비스 HTTP 호출 흉내
        log.info("audit: {}", event)
    }

    override fun log(event: String) {
        scope.launch {
            sendToAuditService(event)
        }
    }
}

fun main() {
    val log = LoggerFactory.getLogger("DomainEventExample")
    val auditor = CoroutineAuditLogger()

    log.info("dispatching domain events...")
    auditor.log("UserCreated(id=1)")
    auditor.log("OrderPlaced(id=42)")
    log.info("returned immediately - audit work is running in background")

    Thread.sleep(300)
}
