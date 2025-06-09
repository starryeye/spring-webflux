package dev.starryeye.logging.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BusinessExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponse> handleBusinessException(BusinessException e) {


        String errorCode = e.getErrorCode().name();
        HttpStatus statusCode = e.getErrorCode().getStatusCode();
        String errorDescription = e.getErrorCode().getDescription();

        String exceptionMessage = e.getMessage();

        log.error("[BusinessException] error code = {}, error status = {}, errer description = {}, message = {}",
                errorCode, statusCode.value(), errorDescription, exceptionMessage, e);

        return ResponseEntity.status(statusCode.value())
                .body(new ExceptionResponse(errorCode, errorDescription));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleRuntimeException(RuntimeException e) {

        String exceptionMessage = e.getMessage();

        log.error("[RuntimeException] message = {}",
                exceptionMessage, e);

        return ResponseEntity.status(500)
                .body(new ExceptionResponse(e.getClass().getName(), e.getMessage()));
    }
}
