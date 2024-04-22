package dev.practice.mapvsflatmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MapvsflatmapApplication {

	/**
	 * Project Reactor 의 map, flatMap 연산자는
	 * 각각 sync, async 성격이 있다고 한다. 직접 실행해보면서 알아보자.
	 */

	public static void main(String[] args) {
		SpringApplication.run(MapvsflatmapApplication.class, args);
	}

}
