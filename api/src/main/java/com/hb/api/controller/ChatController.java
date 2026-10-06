package com.hb.api.controller;

import com.hb.api.dto.ChatRequest;
import com.hb.api.dto.ChatResponse;
import com.hb.api.service.CorneliusAiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final CorneliusAiService corneliusAiService;

    public ChatController(CorneliusAiService corneliusAiService) {
        this.corneliusAiService = corneliusAiService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return corneliusAiService.getResponse(request.getMessage());
    }
}
