package dev.starryeye.logging.common.exception;

public record ExceptionResponse(
        String errorCode,
        String description
) {
}
