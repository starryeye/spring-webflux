package dev.starryeye.hello_coroutine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HelloCoroutineApplication

fun main(args: Array<String>) {
	runApplication<HelloCoroutineApplication>(*args)
}
