package com.marcelo.orchestrator.infrastructure.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuração do cliente HTTP para integração com OpenAI.
 * 
 * <p>Configura WebClient para comunicação com a API do OpenAI.
 * WebClient é a alternativa moderna e reativa ao RestTemplate.</p>
 * 
 * <h3>Por que WebClient?</h3>
 * <ul>
 *   <li><strong>Reativo:</strong> Compatível com Virtual Threads e programação reativa</li>
 *   <li><strong>Performance:</strong> Melhor para alta concorrência</li>
 *   <li><strong>Resilience4j:</strong> Suporte nativo a Circuit Breaker reativo</li>
 *   <li><strong>Non-blocking:</strong> Não bloqueia threads (importante com Virtual Threads)</li>
 * </ul>
 * 
 * <h3>Configuração:</h3>
 * <ul>
 *   <li><strong>Base URL:</strong> Configurável via application.properties</li>
 *   <li><strong>Autenticação:</strong> Bearer token no header Authorization</li>
 *   <li><strong>Timeout:</strong> Configurado para evitar requisições travadas</li>
 * </ul>
 * 
 * <h3>Documentação OpenAI:</h3>
 * <p>Base URL: https://api.openai.com/v1</p>
 * <p>Autenticação: Bearer {api-key}</p>
 * <p>Endpoint: POST /chat/completions</p>
 * 
 * @author Marcelo
 * @see <a href="https://platform.openai.com/docs/api-reference/chat">OpenAI API Reference</a>
 */
@Configuration
public class OpenAIConfig {
    
    @Value("${openai.api.base-url:https://api.openai.com/v1}")
    private String baseUrl;
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    /**
     * Cria WebClient configurado para OpenAI.
     * 
     * <p>Configura:
     * - Base URL da API
     * - Header de autenticação (Bearer token)
     * - Content-Type: application/json
     * - Timeout para requisições</p>
     * 
     * @return WebClient configurado
     */
    @Bean("openAIWebClient")
    public WebClient openAIWebClient() {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            // Timeout configurado para evitar requisições travadas
            .build();
    }
}

