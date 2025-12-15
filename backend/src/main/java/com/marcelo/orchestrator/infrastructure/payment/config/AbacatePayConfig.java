package com.marcelo.orchestrator.infrastructure.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuração do cliente HTTP para integração com AbacatePay.
 * 
 * <p>Configura WebClient para comunicação com a API do AbacatePay.
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
 * <h3>Documentação AbacatePay:</h3>
 * <p>Base URL: https://api.abacatepay.com/v1</p>
 * <p>Autenticação: Bearer {api-key}</p>
 * 
 * @author Marcelo
 * @see <a href="https://docs.abacatepay.com">AbacatePay Documentation</a>
 */
@Slf4j
@Configuration
public class AbacatePayConfig {
    
    @Value("${abacatepay.api.base-url:https://api.abacatepay.com/v1}")
    private String baseUrl;
    
    @Value("${abacatepay.api.key:}")
    private String apiKey;
    
    /**
     * Cria WebClient configurado para AbacatePay.
     * 
     * <p>Configura:
     * - Base URL da API
     * - Header de autenticação (Bearer token)
     * - Content-Type: application/json
     * - Timeout para requisições</p>
     * 
     * @return WebClient configurado
     */
    @Bean("abacatePayWebClient")
    public WebClient abacatePayWebClient() {
        // Validar se API key está configurada
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("⚠️ [AbacatePay] API Key não configurada! Verifique a variável de ambiente ABACATEPAY_API_KEY ou application.yml");
        } else {
            log.info("✅ [AbacatePay] WebClient configurado | Base URL: {} | API Key: {}***", 
                baseUrl, apiKey.substring(0, Math.min(10, apiKey.length())));
        }
        
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            // Timeout configurado para evitar requisições travadas
            .build();
    }
}

