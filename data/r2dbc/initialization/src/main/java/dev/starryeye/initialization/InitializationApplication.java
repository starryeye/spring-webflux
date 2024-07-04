package dev.starryeye.initialization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InitializationApplication {

	/**
	 * todo, r2dbc 를 이용한 DB 연동과 기본 테스트
	 * 	- application.yml 을 이용
	 * 	- Configuration 을 이용
	 */

	public static void main(String[] args) {
		SpringApplication.run(InitializationApplication.class, args);
	}

}
