package dev.practice.reactor.repository;

import dev.practice.common.repository.ArticleEntity;
import dev.practice.common.repository.UserEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Slf4j
public class ArticleReactorRepository {
    private static List<ArticleEntity> articleEntities;

    public ArticleReactorRepository() {
        articleEntities = List.of(
                new ArticleEntity("1", "소식1", "내용1", "1234"),
                new ArticleEntity("2", "소식2", "내용2", "1234"),
                new ArticleEntity("3", "소식3", "내용3", "10000"),
                new ArticleEntity("4", "소식4", "내용4", "1234"),
                new ArticleEntity("5", "소식5", "내용5", "1234"),
                new ArticleEntity("6", "소식6", "내용6", "1234"),
                new ArticleEntity("7", "소식7", "내용7", "1234"),
                new ArticleEntity("8", "소식8", "내용8", "1234"),
                new ArticleEntity("9", "소식9", "내용9", "1234"),
                new ArticleEntity("10", "소식10", "내용10", "1234"),
                new ArticleEntity("11", "소식11", "내용11", "1234")
        );
    }

    @SneakyThrows
    public Flux<ArticleEntity> findAllByUserId(String userId) {

        // 여러 값을 보낼 수 있어야 하므로.. Flux 를 이용한다. Flux.create 로 비동기로 Flux 를 생성한다.
        return Flux.create(sink -> {
            log.info("ArticleRepository.findAllByUserId: {}, tx: {}", userId, Thread.currentThread().getName());

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            articleEntities.stream()
                    .filter(articleEntity -> articleEntity.getUserId().equals(userId))
                    .forEach(sink::next); // 터미널 연산자로 toList 를 사용하지 않고.. forEach 로 sink::Next 를 사용한다.... ㄷㄷ...

            sink.complete(); // 다 흘려보냈으면.. onComplete 이벤트 발생
        });

        // 위 코드는 아래 코드와 동일하다.
//        log.info("ArticleRepository.findAllByUserId: {}", userId);
//
//        try {
//            Thread.sleep(1000); // 1초 지연
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        Stream<ArticleEntity> filtered = articleEntities.stream()
//                .filter(articleEntity -> articleEntity.getUserId().equals(userId));
//        return Flux.fromStream(filtered);
    }

    public Flux<ArticleEntity> findAllWithContext() {

        // deferContextual 을 사용하여 contextView 를 받고 publisher 를 리턴하는 Function 을 쓴다.
        return Flux.deferContextual(contextView -> {
                    Optional<UserEntity> userEntityOptional = contextView.getOrEmpty("user"); // context 에서 key 가 "user" 인 value 를 받는다.

                    if (userEntityOptional.isEmpty()) {
                        // TODO 사실 Optional 처리 할 필요 없는듯..
                        // userRepository.findById 이후 userEntity 없으면.. 하위 흐름은 동작하지 않으므로..
                        // not null 이 보장될 거 같음..
                        throw new RuntimeException("user not found");
                    }

                    return Flux.just(userEntityOptional.get().getId());
                })
                .flatMap(this::findAllByUserId);
    }
}