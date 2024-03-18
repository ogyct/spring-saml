package com.example.springsaml;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestController {

    @GetMapping
    public String hello() {
        return "Hello";
    }

    @GetMapping(path = "/saml-hello")
    public String helloSaml() {
        return "Hello saml";
    }
}
