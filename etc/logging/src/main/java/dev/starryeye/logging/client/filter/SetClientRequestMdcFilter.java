package dev.starryeye.logging.client.filter;

import dev.starryeye.logging.common.ContextMdc;
import dev.starryeye.logging.common.ContextMdcKey;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetClientRequestMdcFilter {

    public ExchangeFilterFunction getFilter() {

        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {

            Map<String, List<String>> puttedHeaders = getMdcHeaders();

            ClientRequest.from(clientRequest)
                    .headers(headers -> headers.putAll(puttedHeaders))
                    .build();

            return Mono.just(clientRequest);
        });
    }

    private Map<String, List<String>> getMdcHeaders() {

        String requestId = ContextMdc.get(ContextMdcKey.REQUEST_ID);

        Map<String, List<String>> puttedHeaders = new HashMap<>();

        if (StringUtils.isNotBlank(requestId)) {
            puttedHeaders.put("X-Request-ID", Collections.singletonList(requestId));
        }

        return puttedHeaders;
    }
}
