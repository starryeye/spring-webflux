package dev.starryeye.logging.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BUSINESS_ERROR_CODE_1(HttpStatus.BAD_REQUEST, "this is error 1");

    private final HttpStatus statusCode;
    private final String description;
}
