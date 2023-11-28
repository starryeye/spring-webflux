package dev.practice.chat.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collation = "chatDocument")
public class ChatDocument {

    @Id
    private final ObjectId id;

    private final String from;
    private final String to;
    private final String message;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PersistenceCreator // document to object mapping 용
    public ChatDocument(ObjectId id, String from, String to, String message, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.message = message;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // save 시점에는 entity 의 objectId 가 null 상태일 것이고.. save 되면 해당 객체의 objectId 에 값을 채워야한다.
    // objectId 는 immutable 변수인데 withId 메서드를 만들어 두면, 채워줄 수 있다.
    public ChatDocument withId(ObjectId objectId) {
        return new ChatDocument(objectId, this.from, this.to, this.message, this.createdAt, this.updatedAt);
    }

    // 편의용
    public static ChatDocument create(String from, String to, String message) {
        return new ChatDocument(null, from, to, message, null, null);
    }
}
