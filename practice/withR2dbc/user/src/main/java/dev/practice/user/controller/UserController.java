package dev.practice.user.controller;

import dev.practice.user.controller.dto.ProfileImageResponse;
import dev.practice.user.controller.dto.UserResponse;
import dev.practice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
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
                                    .map(
                                            user -> new UserResponse(
                                                    user.getId(),
                                                    user.getName(),
                                                    user.getAge(),
                                                    user.getFollowCount(),
                                                    user.getProfileImage()
                                                            .map(
                                                                    image -> new ProfileImageResponse(
                                                                            image.getId(),
                                                                            image.getName(),
                                                                            image.getUrl()
                                                                    )
                                                            )
                                            )
                                    )
                                    .switchIfEmpty( // Mono.empty(), onComplete 이벤트 발생될 경우이다. (= sink.success(빈값) )
                                            // 이걸 안해주면 onComplete 이므로.. 200 ok 에 텅 빈 값이 응답으로 보내진다..
                                            Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)) // WebExceptionHandler 가 ResponseStatusException 을 처리해준다. 404 에러
                                    );
                        }
                );
    }
}
