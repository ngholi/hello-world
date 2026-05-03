package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Hello", description = "Simple test endpoints")
public class HelloController {

    @GetMapping("/hello")
    @Operation(summary = "Hello World", description = "Simple endpoint that returns a greeting")
    public String hello() {
        log.info("Handling hello request");
        return "Hello world";
    }

    @GetMapping("/test-exception")
    @Operation(summary = "Test Exception", description = "Endpoint that throws a runtime exception for testing error handling")
    public String testException() {
        log.info("Handling test-exception request");
        throw new RuntimeException("This is a test exception");
    }
}
