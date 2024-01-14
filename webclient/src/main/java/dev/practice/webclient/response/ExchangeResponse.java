package dev.practice.webclient.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
public class ExchangeResponse {

    private final String result;
    private final String provider;
    private final String documentation;
    private final String termsOfUse;
    private final Instant timeLastUpdateUnix;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEE, dd MMM yyyy HH:mm:ss X", locale = "US") // locale 은 시간 또는 시간대와 상관없다. 요일이나 달 표기가 어떤 언어인지..이다.
    private final ZonedDateTime timeLastUpdateUtc;
    private final Instant timeNextUpdateUnix;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEE, dd MMM yyyy HH:mm:ss X", locale = "US")
    private final ZonedDateTime timeNextUpdateUtc;
    private final Instant timeEolUnix;
    private final String baseCode;
    private final ExchangeRatesResponse rates;

    //"Sun, 14 Jan 2024 00:02:32 +0000",

    @Builder
    private ExchangeResponse(String result, String provider, String documentation, String termsOfUse, Instant timeLastUpdateUnix, ZonedDateTime timeLastUpdateUtc, Instant timeNextUpdateUnix, ZonedDateTime timeNextUpdateUtc, Instant timeEolUnix, String baseCode, ExchangeRatesResponse rates) {
        this.result = result;
        this.provider = provider;
        this.documentation = documentation;
        this.termsOfUse = termsOfUse;
        this.timeLastUpdateUnix = timeLastUpdateUnix;
        this.timeLastUpdateUtc = timeLastUpdateUtc;
        this.timeNextUpdateUnix = timeNextUpdateUnix;
        this.timeNextUpdateUtc = timeNextUpdateUtc;
        this.timeEolUnix = timeEolUnix;
        this.baseCode = baseCode;
        this.rates = rates;
    }

    // 한국 시간으로 변환하는 메서드
    public ZonedDateTime getTimeLastUpdateInKst() {
        return timeLastUpdateUtc.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
    }
}