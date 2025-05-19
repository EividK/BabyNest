package com.assignment_two_starter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * For Testing Purposing to demonstrate SSL Implementation using the web
 */

@RestController
public class helloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
}
