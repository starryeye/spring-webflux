package dev.practice.user.repository;

import dev.practice.user.common.repository.UserEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @DataR2dbcTest 는 DB 연동 특화 Spring 환경 test 이다.
 * 해당 test 에 필요한 빈 들만.. 등록되고 실행된다.
 *
 * todo, 의문점.. r2dbc 환경에서는 spring boot init 시.. db 에 연결할 수 없는 상황이라도 에러 없이 실행이 됨..
 */
@Import(value = R2dbcAuditingConfigForTest.class) // config 적용을 위함
@ActiveProfiles("h2")
@DataR2dbcTest
class UserR2dbcRepositoryTest {

    /**
     * Slice Test (특정 레이어의 Test 를 할때 사용됨, ex. controller, repository)
     */

    @Autowired
    private UserR2dbcRepository userR2dbcRepository; // test 대상

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate; // test 환경 만들어주는 용도

    @AfterEach
    void tearDown() {
        r2dbcEntityTemplate.delete(UserEntity.class)
                .all()
                .block(); // block 을 해야 Test 들 사이에 삭제가 실행됨을 보장할 수 있음
    }

    @Test
    void smoke_test() {
        assertNotNull(userR2dbcRepository);
        assertNotNull(r2dbcEntityTemplate);
    }

    @Test
    void findAll() {
        // library 에서 검증된 메서드 이므로 원래는 test 불필요..

        // given
        // when
        Flux<UserEntity> result = userR2dbcRepository.findAll();

        // then
        StepVerifier.create(result)
                .verifyComplete(); // next 검증이 없으므로.. 텅 비어있는지 test 하는 것이다.
    }

    @Test
    void save() {
        // library 에서 검증된 메서드 이므로 원래는 test 불필요..

        // given
        String name = "tester";
        UserEntity user = TestDataBuilderForRepository.createUnsavedUserEntity(name);

        // when
        Mono<UserEntity> result = userR2dbcRepository.save(user);

        // then
        StepVerifier.create(result)
                .assertNext(
                        savedUser -> {
                            assertNotNull(savedUser.getId()); // db 에 의해 id 가 채워져야한다.
                            assertNotNull(savedUser.getCreatedAt());

                            assertEquals(name, savedUser.getName());

                            assertNotEquals(savedUser, user); // id property 가 immutable 이라 동일하지 않은 엔티티가 반환됨
                        }
                )
                .verifyComplete();
    }

    @Test
    void when_find_all_by_name_starts_with_then_returns_list() {
        // 쿼리 메서드 는 기대한 쿼리로 실행되는지 Test 로 확인하는게 좋다.

        // given
        String prefix = "test";
        Long expectResultCount = 2L;

        UserEntity user1 = TestDataBuilderForRepository.createUnsavedUserEntity("test");
        UserEntity user2 = TestDataBuilderForRepository.createUnsavedUserEntity("tes");
        UserEntity user3 = TestDataBuilderForRepository.createUnsavedUserEntity("testXXX");
        UserEntity user4 = TestDataBuilderForRepository.createUnsavedUserEntity("XXXtest");
        UserEntity user5 = TestDataBuilderForRepository.createUnsavedUserEntity("teXXXst");
        UserEntity user6 = TestDataBuilderForRepository.createUnsavedUserEntity("XXX");

        List<UserEntity> users = List.of(user1, user2, user3, user4, user5, user6);

        // r2dbcEntityTemplate 은 bulk insert 가 없다.
        users.forEach(
                user -> r2dbcEntityTemplate.insert(user)
                        .block()
        );

        // when
        Flux<UserEntity> result = userR2dbcRepository.findAllByNameStartsWith(prefix);

        // then
        StepVerifier.create(result)
                .expectNextCount(expectResultCount)
                .verifyComplete();
    }
}