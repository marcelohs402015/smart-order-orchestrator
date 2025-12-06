package com.marcelo.orchestrator.infrastructure.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO de requisição para a API do OpenAI (Chat Completions).
 * 
 * <p>Representa o formato de dados esperado pela API do OpenAI
 * para análise de risco de pedidos. Utiliza o modelo GPT para
 * análise semântica e classificação de risco.</p>
 * 
 * <h3>Documentação OpenAI:</h3>
 * <p>Endpoint: POST /v1/chat/completions</p>
 * <p>Modelo: gpt-3.5-turbo ou gpt-4 (configurável)</p>
 * 
 * <h3>Estratégia de Prompt:</h3>
 * <p>O prompt é estruturado para que a IA retorne apenas "LOW" ou "HIGH",
 * facilitando o parsing da resposta e garantindo consistência.</p>
 * 
 * @author Marcelo
 * @see <a href="https://platform.openai.com/docs/api-reference/chat">OpenAI API Reference</a>
 */
@Getter
@Builder
public class OpenAIRequest {
    
    /**
     * Modelo a ser utilizado.
     * Exemplos: "gpt-3.5-turbo", "gpt-4", "gpt-4-turbo-preview"
     */
    @JsonProperty("model")
    private String model;
    
    /**
     * Lista de mensagens da conversa.
     * Para análise de risco, contém apenas a mensagem do sistema e do usuário.
     */
    @JsonProperty("messages")
    private List<Message> messages;
    
    /**
     * Temperatura (0.0 a 2.0).
     * Valores mais baixos = respostas mais determinísticas.
     * Para classificação de risco, usar 0.0 ou 0.1 para consistência.
     */
    @JsonProperty("temperature")
    private Double temperature;
    
    /**
     * Número máximo de tokens na resposta.
     * Para classificação simples (LOW/HIGH), 10 tokens é suficiente.
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    /**
     * Representa uma mensagem na conversa com OpenAI.
     */
    @Getter
    @Builder
    public static class Message {
        
        /**
         * Role da mensagem: "system", "user", ou "assistant".
         */
        @JsonProperty("role")
        private String role;
        
        /**
         * Conteúdo da mensagem (prompt).
         */
        @JsonProperty("content")
        private String content;
    }
}

