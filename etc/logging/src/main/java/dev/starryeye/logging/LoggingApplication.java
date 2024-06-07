package dev.starryeye.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class LoggingApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoggingApplication.class, args);
		Hooks.enableAutomaticContextPropagation(); // add hook
	}

	/**
	 * 주의 사항
	 * Hooks.enableAutomaticContextPropagation(); 필수임
	 *
	 * 해당 방법은 spring boot 3, reactor 3.5 버전 이상에서만 가능
	 *
	 * 스레드가 자주 바뀌는 환경에서도 귀찮게 context 를 넘기는 작업 필요 없이 hook 으로 자동으로 넘겨줘서
	 * requestId 가 계속 살아있는다
	 */

}
