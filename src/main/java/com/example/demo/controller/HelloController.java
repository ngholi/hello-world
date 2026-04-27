package com.example.demo.controller;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Observed(name = "hello.controller")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        log.info("Handling hello request");
        return "Hello world";
    }

    @GetMapping("/test-exception")
    public String testException() {
        log.info("Handling test-exception request");
        throw new RuntimeException("This is a test exception");
    }
}
