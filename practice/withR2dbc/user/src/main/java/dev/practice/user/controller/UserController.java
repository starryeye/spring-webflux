package dev.practice.user.controller;

import dev.practice.user.controller.dto.ProfileImageResponse;
import dev.practice.user.controller.dto.SignupUserRequest;
import dev.practice.user.controller.dto.UserResponse;
import dev.practice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public Mono<UserResponse> getUserById(
            @PathVariable("userId") Long userId
    ) {

        return ReactiveSecurityContextHolder.getContext() // WebFilter 에서 넘겨준 SecurityContext 를 Mono 로 받는다.
                .flatMap(
                        securityContext -> {

                            String name = securityContext.getAuthentication().getName(); // WebFilter 에서 채움

                            if (!name.equals(userId.toString())) { // 토큰으로 찾은 name(userId) 와 PathVariable 로 들어온 userId 가 같지 않으면 에러 처리
                                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)); // TODO, WebExceptionHandler 가 처리?
                            }

                            return userService.findById(userId)
                                    .map(UserResponse::of)
                                    .switchIfEmpty( // Mono.empty(), onComplete 이벤트 발생될 경우이다. (= sink.success(빈값) )
                                            // 이걸 안해주면 onComplete 이므로.. 200 ok 에 텅 빈 값이 응답으로 보내진다..
                                            Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)) // WebExceptionHandler 가 ResponseStatusException 을 처리해준다. 404 에러
                                    );
                        }
                );
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public Mono<UserResponse> signupUser(
            @RequestBody SignupUserRequest request
    ) {
        log.info("request={}", request);

        return userService.createUser(request.getName(), request.getAge(), request.getPassword(), request.getProfileImageId())
                .map(UserResponse::of)
                .onErrorMap( // todo, global exception handler... (controller advice..) 로 전환 해보기..
                        RuntimeException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST)
                )
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST)));
    }
}
