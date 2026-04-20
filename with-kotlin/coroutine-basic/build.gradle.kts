plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.starryeye"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// [Kotlin Coroutines] suspend 함수, Flow, Channel 등 Kotlin 네이티브 비동기 모델
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")

	// Coroutine ↔ Reactor (Mono/Flux) 상호 변환 브릿지
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")

	// [Project Reactor] Mono<T> / Flux<T>, 비동기 데이터 스트림
	implementation("io.projectreactor:reactor-core:3.8.4")

	// [RxJava3] Flowable/Observable 등 비동기 스트림
	implementation("io.reactivex.rxjava3:rxjava:3.1.8")
	// Coroutine ↔ RxJava3 브릿지 (Flowable.await() 등)
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx3:1.10.2")

	// [Mutiny] Uni/Multi 비동기 스트림
	implementation("io.smallrye.reactive:mutiny:2.6.0")
	// Coroutine ↔ Mutiny 브릿지 (awaitSuspending() 등)
	implementation("io.smallrye.reactive:mutiny-kotlin:2.6.0")

	// kotlin-logging
	implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<JavaExec> {
	jvmArgs("-Dkotlinx.coroutines.debug")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
