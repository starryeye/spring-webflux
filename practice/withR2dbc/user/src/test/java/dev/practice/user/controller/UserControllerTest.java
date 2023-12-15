package dev.practice.user.controller;

import dev.practice.user.service.AuthService;
import dev.practice.user.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @WebFluxTest 는 servlet stack 의 @WebMvcTest 와 유사하다.
 * controllers 로 명시하지 않으면 default 는 모든 컨트롤러에 대해 진행되어버림
 * Controller advice, filter 등 controller 에 꼭 필요한 빈들이 Spring container 에 등록되고 실행된다.
 */
@WebFluxTest(controllers = UserController.class)
class UserControllerTest {

    /**
     * Slice Test (특정 레이어의 Test 를 할때 사용됨, ex. controller, repository)
     */

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService mockUserService; // UserController 가 의존하는 중

    // @WebFluxTest 는 Filter 도 등록한다. 따라서, 사용자가 만든 SecurityWebFilter 도 등록되어야함.
    // SecurityWebFilter 는 AuthService 를 의존한다.
    @MockBean
    private AuthService mockAuthService;

    @Test
    void webTestClient_should_not_be_null() {
        assertNotNull(webTestClient);
    }

    @Nested // UserService::getUserById 메서드 테스트 전용
    class GetUserById {
        
    }
}