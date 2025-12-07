package com.marcelo.orchestrator.mcp.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Serviço de feedback contextualizado com IA.
 *
 * <p>Usa OpenAI ou Claude para gerar feedback contextualizado sobre código,
 * baseado na análise estática realizada pelo CodeAnalyzer.</p>
 *
 * <h3>Integração com IA:</h3>
 * <ul>
 *   <li>OpenAI GPT-4: Análise de código e sugestões</li>
 *   <li>Claude (futuro): Alternativa para análise</li>
 * </ul>
 *
 * @author Marcelo Hernandes da Silva
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackService {

    private final ChatClient chatClient;

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
            
            String feedback = chatClient.call(new Prompt(prompt))
                .getResult()
                .getOutput()
                .getContent();

            return feedback;

        } catch (Exception e) {
            log.error("Error generating AI feedback", e);
            return "Unable to generate AI feedback: " + e.getMessage();
        }
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

