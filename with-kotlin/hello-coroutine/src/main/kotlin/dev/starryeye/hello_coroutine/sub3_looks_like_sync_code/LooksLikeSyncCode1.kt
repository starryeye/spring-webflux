package dev.starryeye.hello_coroutine.sub3_looks_like_sync_code

import dev.starryeye.hello_coroutine.sub3_looks_like_sync_code.repository.ArticleFutureRepository
import dev.starryeye.hello_coroutine.sub3_looks_like_sync_code.repository.PersonReactorRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import reactor.core.publisher.Mono
import java.util.concurrent.CountDownLatch

/**
 * 코루틴을 사용하지 않고 기존의 Java 스타일이라면..
 *
 * 각 repository 의 반환 값은 Mono, CompletableFuture 로 반환..
 * 기존 Java 의 비동기 스타일이라면 아래 예제와 같이..
 * 		thenApply, map, flatMap 등을 통해 chaining, subscribe 해야한다.
 */

private val log = KotlinLogging.logger {}

fun main() {
	val personRepository = PersonReactorRepository
	val articleRepository = ArticleFutureRepository

	// subscribe는 논블로킹이라 main이 바로 끝나버리므로 latch로 대기
	val latch = CountDownLatch(1)

	personRepository.findPersonByName("person name") // Mono 반환
		.flatMap { person ->
			val future = articleRepository.findArticleById(person.id) // CompletableFuture 반환
			Mono.fromFuture(future)
				.map { article -> person to article }
		}
		.subscribe(
			{ (person, article) ->
				log.info { "person: $person, article: $article" }
				latch.countDown()
			},
			{ error ->
				log.error(error) { "failed" }
				latch.countDown()
			}
		)

	latch.await()
}
