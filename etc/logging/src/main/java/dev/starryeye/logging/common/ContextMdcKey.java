package dev.starryeye.logging.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContextMdcKey {

    LOGGING_TYPE("logging_type"),
    REQUEST_ID("request_id"),
    REQUEST_PATH("request_path"),
    STATUS_CODE("status_code"),
    TOTAL_ELAPSED_TIME("total_elapsed_time"),
    ERROR_BIZ_CODE("error_biz_code"),


    EXTERNAL_API_ELAPSED_TIME("external_api_elapsed_time"),
    EXTERNAL_API_URL("external_api_url"),
    EXTERNAL_API_STATUS_CODE("external_api_status_code"),

    EXTERNAL_DATABASE_ELAPSED_TIME("external_database_elapsed_time"),
    ;

    private final String key;
}
