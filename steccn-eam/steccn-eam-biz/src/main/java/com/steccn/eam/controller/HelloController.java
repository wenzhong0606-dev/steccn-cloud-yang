package com.steccn.eam.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping(value = "/hello", produces = "text/plain")
    public String hello() {
        return "hello eam";
    }
}
