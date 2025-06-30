package dev.starryeye.logging.common.exception;

import dev.starryeye.logging.common.ContextMdc;
import dev.starryeye.logging.common.ContextMdcKey;

public record ExceptionResponse(
        String errorCode,
        String description
) {

    private static final String RESPONSE_FORMAT = "{\"errorCode\":\"%s\",\"description\":\"%s\"}";

    public ExceptionResponse {
        errorDescriptionLogging(errorCode);
    }

    public static String toString(String errorCode, String description) {
        return RESPONSE_FORMAT.formatted(errorCode, description);
    }

    public static void errorDescriptionLogging(String errorCode) {
        /**
         * 예외가 발생할 때만, 만들어지는 errorCode 를 로그로 남기기 위해서..
         * 여기가 최선인것 같다..
         *
         * PrintResponseMdcLogFilter 에서 처리하기 위해서는 너무 많은 코드가 필요하다. (바이트 배열을.. ExceptionResponse 객체로 만들고 errorCode 알아내고 등등..)
         * BusinessExceptionHandler 에서 처리하는 것은.. 취향차이인듯하다..
         *      만약, BusinessExceptionHandler 를 통하지 않고 ExceptionResponse 를 생성해서 그냥 응답할 경우 errorCode 가 로그로 남겨지지 않게 되는 것을 방지하고자 여기서 처리하기로 결정..
         */
        ContextMdc.put(ContextMdcKey.ERROR_BIZ_CODE, errorCode);
    }
}
