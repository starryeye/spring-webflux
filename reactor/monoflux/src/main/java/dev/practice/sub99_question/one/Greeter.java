package dev.practice.sub99_question.one;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Greeter {

    public static String generate() {

        log.info("hello invoke!!");

        return "hello";
    }
}
