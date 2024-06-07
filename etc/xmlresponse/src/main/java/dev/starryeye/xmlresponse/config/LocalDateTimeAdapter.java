package dev.starryeye.xmlresponse.config;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    private final String LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public LocalDateTime unmarshal(String s) {
        return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT));
    }

    @Override
    public String marshal(LocalDateTime localDateTime) throws Exception {
        return localDateTime.format(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT));
//        return localDateTime.toString();
    }
}
