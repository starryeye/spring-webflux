package dev.starryeye.xmlresponse.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebFluxConfigurer {

    private final Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    private final String LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {

        // json serialize, deserialize 설정

        // create ObjectMapper
        ObjectMapper objectMapper = jackson2ObjectMapperBuilder
                .featuresToEnable(SerializationFeature.WRAP_ROOT_VALUE)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT)))
                .build();

        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
    }


}
