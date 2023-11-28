package dev.practice.chat.repository;

import dev.practice.chat.entity.ChatDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ChatMongoRepository extends ReactiveMongoRepository<ChatDocument, ObjectId> {
}
