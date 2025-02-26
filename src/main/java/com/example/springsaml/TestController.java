package com.example.springsaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestController {

    @Autowired
    ObjectMapper objectMapper;

    @GetMapping
    public String hello() {
        return "Hello";
    }

    @GetMapping(path = "/saml-hello")
    @SneakyThrows
    public String helloSaml() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var result = objectMapper.writeValueAsString(principal);
        return result;
    }
}
