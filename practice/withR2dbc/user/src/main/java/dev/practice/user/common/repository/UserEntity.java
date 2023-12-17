package dev.practice.user.common.repository;

import lombok.AllArgsConstructor;
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
    private Long id; // id 는 mutable 로 해야한다..
    // reason.. 생성자로 값을 채울 수 없을 경우(생성자에서 값을 채우지 않음)엔 property population 방법으로 값을 채워준다..
    // property 가 immutable 할 경우엔 채울 수 없음 (by reflection)

    private final String name;
    private final Integer age;
    private final String profileImageId;
    private final String password;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성자는 현재 하나이므로 @PersistenceCreator 를 사용하지 않아도 되긴함.
    //Object mapping 에서 사용할 생성자, property population 을 최소화 한다.
    @PersistenceCreator // property population 을 최소화 하기 위해 여기다가함.. 이러면 AllArgsConstructor 는 필요없는듯?
    public UserEntity(Long id, String name, Integer age, String profileImageId, String password) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.profileImageId = profileImageId;
        this.password = password;
    }

    // for R2dbc
    public static UserEntity create(String name, Integer age, String profileImageId, String password) {
        return new UserEntity(null, name, age, profileImageId, password);
    }
}
