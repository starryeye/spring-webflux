package dev.starryeye.logging.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContextMdcKey {

    LOGGING_TYPE("loggingType"),
    REQUEST_ID("requestId"),

    ;

    private final String key;
}
