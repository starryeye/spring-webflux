package dev.starryeye.coroutine_basic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoroutineBasicApplication

fun main(args: Array<String>) {
	runApplication<CoroutineBasicApplication>(*args)
}
