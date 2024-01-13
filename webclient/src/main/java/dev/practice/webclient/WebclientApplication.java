package dev.practice.webclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
	 */

	public static void main(String[] args) {
		SpringApplication.run(WebclientApplication.class, args);
	}

}
