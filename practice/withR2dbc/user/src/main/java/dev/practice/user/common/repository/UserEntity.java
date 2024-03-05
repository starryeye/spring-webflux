package dev.practice.user.common.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

//@AllArgsConstructor
@Table("USERS")
@Data
public class UserEntity {

    @Id
    private final Long id; // immutable 이므로 saved 객체는 새로운 객체로 리턴된다. (mutable 하면, save 전과 후가 동일할 수 도 있음)

    private final String name;
    private final Integer age;
    private final String profileImageId;
    private final String password;

    @CreatedDate
    private final LocalDateTime createdAt;

    @LastModifiedDate
    private final LocalDateTime updatedAt;

    // 유일한 생성자 + AllArgsConstructor
    // 해당 생성자로 Object mapping (Object create + property population) 을 진행한다.
    @Builder
    private UserEntity(Long id, String name, Integer age, String profileImageId, String password, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.profileImageId = profileImageId;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserEntity create(String name, Integer age, String profileImageId, String password) {
        return UserEntity.builder()
                .id(null)
                .name(name)
                .age(age)
                .profileImageId(profileImageId)
                .password(password)
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    // for test
    public static UserEntity createWithId(Long id, String name, Integer age, String profileImageId, String password) {
        return UserEntity.builder()
                .id(id)
                .name(name)
                .age(age)
                .profileImageId(profileImageId)
                .password(password)
                .createdAt(null)
                .updatedAt(null)
                .build();
    }
}
