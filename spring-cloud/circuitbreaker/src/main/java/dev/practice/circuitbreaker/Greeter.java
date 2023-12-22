package dev.practice.circuitbreaker;

import org.springframework.stereotype.Component;

@Component
public class Greeter {

    /**
     * spy bean 등록하여 검증을 위해 따로 객체로 뺐다.
     */

    private final String MESSAGE = "hello, %s!";

    public String generate(String to) {
        return MESSAGE.formatted(to);
    }
}
