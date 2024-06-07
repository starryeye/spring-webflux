package dev.starryeye.xmlresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XmlresponseApplication {

	/**
	 *
	 * 관련 링크
	 * https://github.com/spring-projects/spring-framework/issues/20256
	 * https://stackoverflow.com/questions/55306194/springboot-webflux-cannot-return-application-xml
	 *
	 * spring reactive stack 에서는 servlet stack 과 다르게 jackson-dataformat-xml 를 지원하지 않아서
	 * jaxb 를 사용해야한다.
	 *
	 * todo
	 * 	Accept 가 안들어왔을 때.. json 이 기본인데.. xml 로 하는 방법은?
	 */

	public static void main(String[] args) {
		SpringApplication.run(XmlresponseApplication.class, args);
	}

}
