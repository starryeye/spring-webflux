package dev.practice.user.controller;

import dev.practice.user.controller.dto.UserResponse;
import dev.practice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public Mono<UserResponse> getUserById(
        @PathVariable("userId") String userId
    )  {
        return userService.findById(userId)
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getAge(), user.getFollowCount()))
                .switchIfEmpty( // Mono.empty(), onComplete 이벤트 발생될 경우이다. (= sink.success(빈값) )
                        // 이걸 안해주면 onComplete 이므로.. 200 ok 에 텅 빈 값이 응답으로 보내진다..
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)) // WebExceptionHandler 가 ResponseStatusException 을 처리해준다. 404 에러
                );
    }
}
