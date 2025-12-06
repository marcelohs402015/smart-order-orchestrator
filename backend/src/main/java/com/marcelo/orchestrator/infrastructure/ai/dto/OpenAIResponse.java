package com.marcelo.orchestrator.infrastructure.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO de resposta da API OpenAI (Chat Completions).
 * 
 * <p>Representa o formato de resposta padrão do OpenAI.
 * A resposta contém a classificação de risco (LOW ou HIGH)
 * no campo choices[0].message.content.</p>
 * 
 * <h3>Estrutura de Resposta OpenAI:</h3>
 * <pre>
 * {
 *   "id": "chatcmpl-123",
 *   "object": "chat.completion",
 *   "created": 1677652288,
 *   "choices": [{
 *     "index": 0,
 *     "message": {
 *       "role": "assistant",
 *       "content": "LOW"
 *     },
 *     "finish_reason": "stop"
 *   }],
 *   "usage": {
 *     "prompt_tokens": 9,
 *     "completion_tokens": 12,
 *     "total_tokens": 21
 *   }
 * }
 * </pre>
 * 
 * <h3>Mapeamento para Domínio:</h3>
 * <p>Este DTO é convertido para {@code RiskAnalysisResult} do domínio
 * pelo adapter, isolando detalhes da API externa.</p>
 * 
 * @author Marcelo
 * @see <a href="https://platform.openai.com/docs/api-reference/chat">OpenAI API Reference</a>
 */
@Getter
@Setter
public class OpenAIResponse {
    
    /**
     * ID único da resposta.
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * Tipo do objeto retornado.
     */
    @JsonProperty("object")
    private String object;
    
    /**
     * Timestamp de criação.
     */
    @JsonProperty("created")
    private Long created;
    
    /**
     * Lista de escolhas (respostas) geradas.
     */
    @JsonProperty("choices")
    private List<Choice> choices;
    
    /**
     * Informações de uso (tokens consumidos).
     */
    @JsonProperty("usage")
    private Usage usage;
    
    /**
     * Verifica se a resposta indica sucesso e contém conteúdo.
     * 
     * @return {@code true} se choices não é vazio e contém mensagem
     */
    public boolean isSuccess() {
        return choices != null && !choices.isEmpty() 
            && choices.get(0) != null 
            && choices.get(0).getMessage() != null
            && choices.get(0).getMessage().getContent() != null;
    }
    
    /**
     * Extrai o conteúdo da primeira escolha (resposta da IA).
     * 
     * @return Conteúdo da resposta (ex: "LOW" ou "HIGH")
     */
    public String getContent() {
        if (!isSuccess()) {
            return null;
        }
        return choices.get(0).getMessage().getContent().trim();
    }
    
    /**
     * Representa uma escolha (resposta) gerada pela IA.
     */
    @Getter
    @Setter
    public static class Choice {
        
        /**
         * Índice da escolha.
         */
        @JsonProperty("index")
        private Integer index;
        
        /**
         * Mensagem gerada pela IA.
         */
        @JsonProperty("message")
        private Message message;
        
        /**
         * Razão pela qual a geração parou.
         * Valores: "stop", "length", "content_filter", etc.
         */
        @JsonProperty("finish_reason")
        private String finishReason;
    }
    
    /**
     * Representa uma mensagem na resposta.
     */
    @Getter
    @Setter
    public static class Message {
        
        /**
         * Role da mensagem (geralmente "assistant").
         */
        @JsonProperty("role")
        private String role;
        
        /**
         * Conteúdo da mensagem (resposta da IA).
         * Exemplo: "LOW" ou "HIGH"
         */
        @JsonProperty("content")
        private String content;
    }
    
    /**
     * Informações sobre uso de tokens.
     */
    @Getter
    @Setter
    public static class Usage {
        
        /**
         * Tokens usados no prompt.
         */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        
        /**
         * Tokens usados na resposta.
         */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        
        /**
         * Total de tokens usados.
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}

