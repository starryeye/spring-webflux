package dev.starryeye.logging.common.exception;

public record ExceptionResponse(
        String errorCode,
        String description
) {

    private static final String RESPONSE_FORMAT = "{\"errorCode\":\"%s\",\"description\":\"%s\"}";

    public static String toString(String errorCode, String description) {
        return RESPONSE_FORMAT.formatted(errorCode, description);
    }
}
