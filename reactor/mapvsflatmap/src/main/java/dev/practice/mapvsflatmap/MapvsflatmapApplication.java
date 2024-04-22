package dev.practice.mapvsflatmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MapvsflatmapApplication {

	/**
	 * Project Reactor 의 map, flatMap 연산자는
	 * 각각 sync, async 성격이 있다고 한다. 직접 실행해보면서 알아보자.
	 *
	 *
	 * flatMap
	 * 	Transform the item emitted by this Mono "asynchronously",
	 * 	returning the value emitted by another Mono (possibly changing the value type).
	 *
	 * map
	 * 	Transform the item emitted by this Mono by applying a "synchronous" function to it.
	 */

	public static void main(String[] args) {
		SpringApplication.run(MapvsflatmapApplication.class, args);
	}

}
