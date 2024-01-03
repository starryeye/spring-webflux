package dev.practice.stream.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PrintService {

    public void print(String format, Object arg) {

        log.info(format, arg);
    }
}
