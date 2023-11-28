package dev.practice.chat;

import com.mongodb.client.model.changestream.OperationType;
import dev.practice.chat.entity.ChatDocument;
import dev.practice.chat.repository.ChatMongoRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Slf4j
@SpringBootApplication
public class ChatApplication {

	@Autowired
	private ChatMongoRepository chatMongoRepository;

	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(ChatApplication.class, args);
	}

	@Bean
	ApplicationRunner runner() {
		return args -> {

			log.info("start test, tx={}", Thread.currentThread().getName());

			// mongodb 의 변경을 감지하는 flux
			reactiveMongoTemplate.changeStream(ChatDocument.class)
					.listen()
					.doOnNext(
							item -> {
								ChatDocument target = item.getBody();
								OperationType operationType = item.getOperationType();
								BsonValue resumeToken = item.getResumeToken();

								log.info("target={}, tx={}", target, Thread.currentThread().getName());
								log.info("operationType={}, tx={}", operationType, Thread.currentThread().getName());
								log.info("resumeToken={}, tx={}", resumeToken, Thread.currentThread().getName());
							}
					).subscribe();

			Thread.sleep(1000); // 스레드 슬립 1초

			ChatDocument newChat = ChatDocument.create("a", "b", "hello");

			// mongodb 에 entity(document) insert
			chatMongoRepository.save(newChat)
					.doOnNext(
							saved -> log.info("saved={}, tx={}", saved, Thread.currentThread().getName())
					).subscribe();

			log.info("end test, tx={}", Thread.currentThread().getName());

			/**
			 * 실행 해보면, reactive stream (pipeline) 수행도 nioEventLoopGroup 이 하는 것을 알 수 있다.
			 * 이에 따라 IO 실행은 nioEventLoopGroup 이 수행하는 것을 알 수 있다.
			 * 즉, main 은 비동기 non-blocking 으로 동작함.
			 *
			 * 위 test 동작은
			 * 미리 특정 collection 의 변경 감지를 flux 로 등록(listen) 하고
			 * 1초 후에 해당 collection 에 document 를 하나 insert 하니까
			 * 해당 flux 에서 변경이 감지 되어 item 이 전달됨.
			 */
		};
	}
}
