package dev.practice.circuitbreaker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Greeter {

    /**
     * spy bean 등록하여 검증을 위해 따로 객체로 뺐다.
     */

    private final String MESSAGE = "hello, %s!";

    public String generate(String to) {

        log.info("generate invoke !!");

        return MESSAGE.formatted(to);
    }
}
