package dev.starryeye.hello_coroutine.sub3_looks_like_sync_code

import dev.starryeye.hello_coroutine.sub3_looks_like_sync_code.repository.ArticleFutureRepository
import dev.starryeye.hello_coroutine.sub3_looks_like_sync_code.repository.PersonReactorRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking

/**
 * Kotlin coroutine 을 사용하게 되면..
 *
 * runBlocking 과 suspend 함수 (awaitSingle, await) 를 통해서 동기 스타일로 보이게 할 수 있다.
 *      실제 런타임은 non-blocking 으로 동작한다.
 */
private val log = KotlinLogging.logger {}

fun main() = runBlocking {
    val personRepository = PersonReactorRepository
    val articleRepository = ArticleFutureRepository

    val person = personRepository.findPersonByName("person name") // Mono 반환
        .awaitSingle() // Mono<Person> -> Person

    val article = articleRepository.findArticleById(person.id) // CompletableFuture 반환
        .await() // CompletableFuture<Article> -> Article

    log.info { "person: $person, article: $article" }
}