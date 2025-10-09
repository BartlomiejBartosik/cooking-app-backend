package org.example.cookingappbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SecureTestController {
    @GetMapping("/secure")
    public String secure() { return "OK secured"; }
}