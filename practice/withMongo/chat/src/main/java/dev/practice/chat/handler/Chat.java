package dev.practice.chat.handler;

import dev.practice.chat.entity.ChatDocument;
import lombok.Data;

@Data
public class Chat {

    private final String from;
    private final String to;
    private final String message;

    public ChatDocument toEntity() {
        return ChatDocument.create(from, to, message);
    }
}
