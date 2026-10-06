package com.hb.api.service;

import com.hb.api.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CorneliusAiService {

    @Value("${ollama.api.url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    @Value("${gemini.api.url}")
    private String geminiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public ChatResponse getResponse(String prompt) {
        try {
            return callOllama(prompt);
        } catch (Exception e) {
            System.err.println("Erro ao chamar Ollama (tentando fallback para Gemini): " + e.getMessage());
            try {
                return callGemini(prompt);
            } catch (Exception ex) {
                System.err.println("Erro no Gemini também: " + ex.getMessage());
                return new ChatResponse("Desculpe, estou enfrentando problemas técnicos.", "Error");
            }
        }
    }

    public String evaluatePriority(String title, String description) {
        String prompt = "Baseado no seguinte título e descrição de um chamado de suporte de TI, classifique a prioridade estritamente como uma das opções: BAIXA, MEDIA, ALTA, ou CRITICA. Não escreva mais nada, apenas a palavra. Título: " + title + " | Descrição: " + description;
        try {
            ChatResponse res = getResponse(prompt);
            String ans = res.getResponse().toUpperCase().trim();
            if (ans.contains("CRITICA") || ans.contains("CRÍTICA")) return "CRITICA";
            if (ans.contains("ALTA")) return "ALTA";
            if (ans.contains("MEDIA") || ans.contains("MÉDIA")) return "MEDIA";
            return "BAIXA";
        } catch(Exception e) {
            return "MEDIA"; // Default em caso de erro extremo
        }
    }

    private ChatResponse callOllama(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl, entity, Map.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String aiText = (String) response.getBody().get("response");
            return new ChatResponse(aiText, "Ollama");
        }
        throw new RuntimeException("Ollama retornou erro HTTP: " + response.getStatusCode());
    }

    private ChatResponse callGemini(String prompt) {
        if (geminiApiKey == null || geminiApiKey.isEmpty() || "COLOQUE_SUA_CHAVE_AQUI".equals(geminiApiKey)) {
            throw new RuntimeException("Chave da API do Gemini não configurada.");
        }

        String url = geminiUrl + "?key=" + geminiApiKey;

        // Montando o payload do Gemini API
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(parts));
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            try {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                Map<String, Object> resContent = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> resParts = (List<Map<String, Object>>) resContent.get("parts");
                String aiText = (String) resParts.get(0).get("text");
                return new ChatResponse(aiText, "Gemini");
            } catch (Exception e) {
                throw new RuntimeException("Erro ao processar resposta do Gemini", e);
            }
        }
        throw new RuntimeException("Gemini retornou erro HTTP: " + response.getStatusCode());
    }
}
