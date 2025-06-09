package dev.starryeye.logging.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContextMdcKey {

    LOGGING_TYPE("logging_type"),
    REQUEST_ID("request_id"),

    ;

    private final String key;
}
