package dev.starryeye.logging.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContextMdcValue {

    LOGGING_TYPE_REQUEST("REQUEST"),
    LOGGING_TYPE_BUSINESS("BUSINESS"),
    ;

    private final String value;
}
