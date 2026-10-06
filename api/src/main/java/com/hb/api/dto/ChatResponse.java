package com.hb.api.dto;

public class ChatResponse {
    private String response;
    private String source; // "Ollama" ou "Gemini"

    public ChatResponse() {}

    public ChatResponse(String response, String source) {
        this.response = response;
        this.source = source;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
