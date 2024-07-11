package dev.starryeye.initialization;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public abstract class BaseEntity {

    @CreatedDate
    private final LocalDateTime createdAt;

    @LastModifiedDate
    private final LocalDateTime updatedAt;
}
