package com.marcelo.orchestrator.mcp.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuração do cliente HTTP para integração com OpenAI.
 * 
 * @author Marcelo Hernandes da Silva
 */
@Configuration
public class OpenAIConfig {
    
    @Value("${openai.api.base-url:https://api.openai.com/v1}")
    private String baseUrl;
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    @Bean("openAIWebClient")
    public WebClient openAIWebClient() {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}

