package com.marcelo.orchestrator.presentation.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de resposta para erros da API.
 * 
 * <p>Formato padronizado de erro retornado pela API REST.
 * Segue padrão RFC 7807 (Problem Details for HTTP APIs).</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class ErrorResponse {
    
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> details; // Para erros de validação
}

