package dev.practice.user.runner;

import dev.practice.user.common.repository.UserEntity;
import dev.practice.user.repository.UserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring 실행 때, CommandLineRunner 를 구현한 빈이 등록 되면
 * run 메서드를 호출한다.
 *
 * -> @PostConstructor 와
 * @Bean
 * ApplicationRunner run(RestTemplate restTemplate) {
 *     return args -> { something };
 * }
 *
 * 비슷
 */
@Slf4j
@RequiredArgsConstructor
//@Component
public class UserInsertRunner implements CommandLineRunner {

    private final UserR2dbcRepository userR2dbcRepository;
    @Override
    public void run(String... args) throws Exception {

        UserEntity user = new UserEntity("starryeye", 20, "1", "1q2w3e4r!");

        // user 와 saved 는 동일한 객체이다.
        UserEntity saved = userR2dbcRepository.save(user).block(); // todo, block mean..


        log.info("savedUser={}", saved);
    }
}
