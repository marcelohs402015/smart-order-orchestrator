package com.marcelo.orchestrator.presentation.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de resposta para erros da API.
 * 
 * <p>Formato padronizado de erro retornado pela API REST.
 * Segue padrão RFC 7807 (Problem Details for HTTP APIs).</p>
 * 
 * <h3>Por que Record?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Dados não podem ser alterados após criação</li>
 *   <li><strong>Simplicidade:</strong> Menos código, mais legível (Java 17+)</li>
 *   <li><strong>Performance:</strong> Menos overhead que classes tradicionais</li>
 * </ul>
 * 
 * @param timestamp Data e hora do erro
 * @param status Código HTTP do erro
 * @param error Tipo do erro
 * @param message Mensagem descritiva do erro
 * @param details Detalhes adicionais (para erros de validação)
 * 
 * @author Marcelo
 */
public record ErrorResponse(
    @JsonProperty("timestamp")
    LocalDateTime timestamp,
    
    @JsonProperty("status")
    int status,
    
    @JsonProperty("error")
    String error,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("details")
    Map<String, String> details // Para erros de validação
) {
}
