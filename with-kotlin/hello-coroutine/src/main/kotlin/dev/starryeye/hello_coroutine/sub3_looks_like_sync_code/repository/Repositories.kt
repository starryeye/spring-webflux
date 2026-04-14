package dev.starryeye.hello_coroutine.sub3_looks_like_sync_code.repository

import dev.starryeye.hello_coroutine.sub3_looks_like_sync_code.domain.Article
import dev.starryeye.hello_coroutine.sub3_looks_like_sync_code.domain.Person
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom

object PersonReactorRepository { // 싱글톤

    fun findPersonByName(name: String): Mono<Person> { // Mono 반환
        // 실제로는 DB/외부호출. 여기서는 지연 후 생성된 id로 Person 반환
        return Mono.fromSupplier {
            val id = ThreadLocalRandom.current().nextLong(1000, 9999)
            Person(id = id, name = name)
        }.delayElement(Duration.ofMillis(100))
    }
}

object ArticleFutureRepository { // 싱글톤

    fun findArticleById(id: Long): CompletableFuture<Article> { // CompletableFuture 반환
        // 실제로는 DB/외부호출. 여기서는 지연 후 생성된 id로 Article 반환
        return CompletableFuture.supplyAsync {
            Thread.sleep(100)
            Article(id = id, title = "article $id")
        }
    }
}