package com.marcelo.orchestrator.mcp.ai;

import com.marcelo.orchestrator.mcp.ai.dto.OpenAIRequest;
import com.marcelo.orchestrator.mcp.ai.dto.OpenAIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Serviço de feedback contextualizado com IA.
 *
 * <p>Usa OpenAI para gerar feedback contextualizado sobre código,
 * baseado na análise estática realizada pelo CodeAnalyzer.</p>
 *
 * <h3>Integração com IA:</h3>
 * <ul>
 *   <li>OpenAI GPT-3.5/GPT-4: Análise de código e sugestões</li>
 *   <li>Claude (futuro): Alternativa para análise</li>
 * </ul>
 *
 * @author Marcelo Hernandes da Silva
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackService {

    @Qualifier("openAIWebClient")
    private final WebClient openAIWebClient;

    @Value("${openai.api.model:gpt-3.5-turbo}")
    private String openaiModel;

    @Value("${openai.api.temperature:0.3}")
    private Double temperature;

    @Value("${openai.api.max-tokens:1000}")
    private Integer maxTokens;

    /**
     * Gera feedback contextualizado com IA baseado na análise estática.
     *
     * @param code Código fonte
     * @param analysis Resultado da análise estática
     * @param focus Foco da análise
     * @return Feedback contextualizado
     */
    public String generateFeedback(String code, Map<String, Object> analysis, String focus) {
        try {
            String prompt = buildPrompt(code, analysis, focus);
            
            OpenAIRequest request = buildOpenAIRequest(prompt);
            
            OpenAIResponse response = openAIWebClient
                .post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAIResponse.class)
                .block(); // Bloqueia para retorno síncrono
            
            if (response != null && response.isSuccess()) {
                return response.getContent();
            } else {
                log.warn("Invalid response from OpenAI");
                return "Unable to generate AI feedback: Invalid response from OpenAI";
            }

        } catch (WebClientResponseException e) {
            log.error("OpenAI API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "Unable to generate AI feedback: OpenAI API error - " + e.getStatusCode();
        } catch (Exception e) {
            log.error("Error generating AI feedback", e);
            return "Unable to generate AI feedback: " + e.getMessage();
        }
    }
    
    /**
     * Constrói OpenAIRequest a partir do prompt.
     */
    private OpenAIRequest buildOpenAIRequest(String prompt) {
        OpenAIRequest.Message systemMessage = OpenAIRequest.Message.builder()
            .role("system")
            .content("You are an expert Java code reviewer. Provide constructive feedback on code quality, design patterns, and best practices.")
            .build();
        
        OpenAIRequest.Message userMessage = OpenAIRequest.Message.builder()
            .role("user")
            .content(prompt)
            .build();
        
        return OpenAIRequest.builder()
            .model(openaiModel)
            .messages(List.of(systemMessage, userMessage))
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();
    }

    private String buildPrompt(String code, Map<String, Object> analysis, String focus) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert Java code reviewer. Analyze the following code and provide constructive feedback.\n\n");
        prompt.append("Focus: ").append(focus).append("\n\n");
        prompt.append("Code:\n```java\n").append(code).append("\n```\n\n");
        
        if (analysis.containsKey("violations")) {
            prompt.append("Static Analysis Results:\n");
            prompt.append(analysis.get("violations")).append("\n\n");
        }

        prompt.append("Please provide:\n");
        prompt.append("1. Overall assessment\n");
        prompt.append("2. Strengths\n");
        prompt.append("3. Areas for improvement\n");
        prompt.append("4. Specific suggestions\n");

        return prompt.toString();
    }
}

