package dev.practice.webclient.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ExchangeResponse {

    private final String result;
    private final String provider;
    private final String documentation;
    private final String termsOfUse;
    private final String timeLastUpdateUnix;
    private final String timeLastUpdateUtc;
    private final String timeNextUpdateUnix;
    private final String timeNextUpdateUtc;
    private final String timeEolUnix;
    private final String baseCode;
    private final ExchangeRatesResponse rates;

    @Builder
    private ExchangeResponse(String result, String provider, String documentation, String termsOfUse, String timeLastUpdateUnix, String timeLastUpdateUtc, String timeNextUpdateUnix, String timeNextUpdateUtc, String timeEolUnix, String baseCode, ExchangeRatesResponse rates) {
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
}