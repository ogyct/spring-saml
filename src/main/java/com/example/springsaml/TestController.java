package com.example.springsaml;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
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
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Saml2AuthenticatedPrincipal saml2AuthenticatedPrincipal) {
            return saml2AuthenticatedPrincipal.getAttributes().toString();
        }
        return principal.toString();
    }
}
