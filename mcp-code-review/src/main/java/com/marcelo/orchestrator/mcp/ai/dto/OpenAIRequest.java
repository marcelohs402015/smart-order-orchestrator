package com.marcelo.orchestrator.mcp.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO de requisição para a API do OpenAI (Chat Completions).
 * 
 * <p>Representa o formato de dados esperado pela API do OpenAI
 * para análise de código e geração de feedback.</p>
 * 
 * @author Marcelo Hernandes da Silva
 */
@Getter
@Builder
public class OpenAIRequest {
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("messages")
    private List<Message> messages;
    
    @JsonProperty("temperature")
    private Double temperature;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    @Getter
    @Builder
    public static class Message {
        @JsonProperty("role")
        private String role;
        
        @JsonProperty("content")
        private String content;
    }
}

