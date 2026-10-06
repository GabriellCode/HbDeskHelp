package com.hb.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AppController {

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("status", "UP", "message", "HbDeskHelp API is running.");
    }
}
