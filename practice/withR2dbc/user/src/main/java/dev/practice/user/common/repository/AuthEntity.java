package dev.practice.user.common.repository;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("AUTH")
@Data
public class AuthEntity {

    @Id
    private Long id;

    private final Long userId;
    private final String token;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성자는 현재 하나이므로 @PersistenceCreator 를 사용하지 않아도 되긴함.
    @PersistenceCreator //Object mapping 에서 사용할 생성자, property population 을 최소화 한다.
    public AuthEntity(Long id, Long userId, String token) {
        this.id = id;
        this.userId = userId;
        this.token = token;
    }

    public static AuthEntity create(Long userId, String token) {
        return new AuthEntity(null, userId, token);
    }
}
