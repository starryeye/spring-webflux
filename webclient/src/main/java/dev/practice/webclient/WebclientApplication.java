package dev.practice.webclient;

import dev.practice.webclient.client.ExchangeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class WebclientApplication {

	/**
	 * WebClient 는 Spring MVC, Spring WebFlux 모두 사용 가능하다.
	 *
	 * RestTemplate
	 * - 동기 blocking 기반의 web client
	 * - Spring 5.0 부터 유지모드
	 * - WebClient 사용을 권장함
	 *
	 * WebClient
	 * - non-blocking reactive http 클라이언트
	 * - Reactor Netty, Jetty, apache 의 HttpComponent 를 이용하여 구현
	 * - 메서드 체이닝
	 *
	 *
	 * 해당 프로젝트에 Timeout 관련 설정법을 다루는데 그냥 circuit breaker 사용하는게 맞는듯
	 *
	 *
	 * todo
	 * 	1. post() request body
	 * 	2. flux response
	 */

	private final ExchangeClient exchangeClient;

	public static void main(String[] args) {
		SpringApplication.run(WebclientApplication.class, args);
	}


	@Bean
	public ApplicationRunner runner() {
		return args -> {

			exchangeClient.helloCall()
					.doOnNext(
							exchangeResponse -> {
								log.info("result = {}, krw = {}", exchangeResponse.getResult(), exchangeResponse.getRates().getKRW());
							}
					)
					.subscribe();
		};
	}
}
