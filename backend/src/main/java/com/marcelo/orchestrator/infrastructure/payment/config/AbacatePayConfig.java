package com.marcelo.orchestrator.infrastructure.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;


@Slf4j
@Configuration
public class AbacatePayConfig {
    
    @Value("${abacatepay.api.base-url:https://api.abacatepay.com/v1}")
    private String baseUrl;
    
    @Value("${abacatepay.api.key:}")
    private String apiKey;
    
    
    @Bean("abacatePayWebClient")
    public WebClient abacatePayWebClient() {
        
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
            
            .build();
    }
}

