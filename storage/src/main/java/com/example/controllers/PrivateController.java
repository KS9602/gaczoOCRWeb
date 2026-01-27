package com.example.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/private")
public class PrivateController {

    @GetMapping("/test")
    public String test() {
        return "test prywatny";
    }
}
