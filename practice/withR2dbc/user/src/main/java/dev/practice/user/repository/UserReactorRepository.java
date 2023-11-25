package dev.practice.user.repository;

import dev.practice.user.common.repository.UserEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Repository
public class UserReactorRepository {
    private final Map<String, UserEntity> userMap;

    public UserReactorRepository() {
        var user = new UserEntity("1234", "starryeye", 20, "1");

        userMap = Map.of("1234", user);
    }

    @SneakyThrows
    public Mono<UserEntity> findById(String userId) {

        // Mono 에서 sequence 를 만드는 create(Consumer<MonoSink<T>> callback) 사용, 비동기로 Mono 를 생성한다.
        return Mono.create(sink -> {
            log.info("UserRepository.findById: {}, tx: {}", userId, Thread.currentThread().getName());

            try {
                Thread.sleep(1000); // 1초 지연
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            UserEntity user = userMap.get(userId);
            if(user == null) {
                // 값 없음, onComplete 이벤트 전파
                // findById Caller 입장에서는 Mono.empty() 와 동일하다.
                sink.success();
            }else {
                // 값 전달, onComplete 이벤트 전파
                sink.success(user);
            }
        });

        // 위 코드는 아래 코드와 동일하다.
//        log.info("UserRepository.findById: {}", userId);
//        try {
//            Thread.sleep(1000); // 1초 지연
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        UserEntity user = userMap.get(userId);
//        return Mono.justOrEmpty(user);
    }
}