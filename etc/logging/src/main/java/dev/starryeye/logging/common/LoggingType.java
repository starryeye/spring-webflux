package dev.starryeye.logging.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoggingType {

    REQUEST("요청 시점"),
    RESPONSE("응답 시점"),

    BUSINESS("비즈니스 시점"),

    CLIENT_REQUEST("외부로 요청 시점"),
    CLIENT_RESPONSE("외부에서 응답 시점"),
    ;

    private final String description;
}
