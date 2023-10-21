package dev.practice.user.service;

import dev.practice.user.common.domain.User;
import dev.practice.user.repository.UserReactorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserReactorRepository userReactorRepository;

    public Mono<User> findById(String userId) {

        return userReactorRepository.findById(userId)
                .map(
                        userEntity -> new User(
                                userEntity.getId(),
                                userEntity.getName(),
                                userEntity.getAge(),
                                Optional.empty(),
                                List.of(),
                                0L
                        )
                );

    }
}
