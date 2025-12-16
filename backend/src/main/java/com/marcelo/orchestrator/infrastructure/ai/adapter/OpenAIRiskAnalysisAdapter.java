package com.marcelo.orchestrator.infrastructure.ai.adapter;

import com.marcelo.orchestrator.domain.model.RiskLevel;
import com.marcelo.orchestrator.domain.port.RiskAnalysisPort;
import com.marcelo.orchestrator.domain.port.RiskAnalysisRequest;
import com.marcelo.orchestrator.domain.port.RiskAnalysisResult;
import com.marcelo.orchestrator.infrastructure.ai.dto.OpenAIRequest;
import com.marcelo.orchestrator.infrastructure.ai.dto.OpenAIResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * Adaptador que implementa RiskAnalysisPort usando OpenAI.
 * 
 * <p>Este é o <strong>Adapter</strong> na Arquitetura Hexagonal.
 * Implementa a porta definida no domínio usando WebClient para comunicação HTTP
 * com a API do OpenAI (Chat Completions).</p>
 * 
 * <h3>Arquitetura Hexagonal:</h3>
 * <ul>
 *   <li><strong>Port:</strong> RiskAnalysisPort (definida no domínio)</li>
 *   <li><strong>Adapter:</strong> Esta classe (implementa a porta)</li>
 *   <li><strong>Inversão de Dependência:</strong> Domínio não conhece esta implementação</li>
 * </ul>
 * 
 * <h3>Resiliência com Resilience4j:</h3>
 * <ul>
 *   <li><strong>@CircuitBreaker:</strong> Protege contra falhas em cascata</li>
 *   <li><strong>@Retry:</strong> Tenta novamente em falhas transitórias</li>
 *   <li><strong>Fallback:</strong> Retorna PENDING quando OpenAI está indisponível</li>
 * </ul>
 * 
 * <h3>Estratégia de Prompt:</h3>
 * <p>O prompt é estruturado para que a IA retorne apenas "LOW" ou "HIGH",
 * facilitando o parsing e garantindo consistência. O prompt inclui:</p>
 * <ul>
 *   <li>Instruções claras sobre o que analisar</li>
 *   <li>Formato de resposta esperado</li>
 *   <li>Contexto do pedido e cliente</li>
 * </ul>
 * 
 * <h3>Documentação OpenAI:</h3>
 * <p>Base URL: https://api.openai.com/v1</p>
 * <p>Autenticação: Bearer token no header Authorization</p>
 * <p>Endpoint: POST /chat/completions</p>
 * 
 * @author Marcelo
 * @see <a href="https://platform.openai.com/docs/api-reference/chat">OpenAI API Reference</a>
 */
@Slf4j
@Component
public class OpenAIRiskAnalysisAdapter implements RiskAnalysisPort {
    
    private final WebClient openAIWebClient;
    private final String model;
    private final Double temperature;
    private final Integer maxTokens;
    
    /**
     * Construtor com injeção de dependências.
     * 
     * <p>Padrão: Constructor Injection - campos final garantem imutabilidade
     * e seguem boas práticas do Spring.</p>
     */
    public OpenAIRiskAnalysisAdapter(
            WebClient openAIWebClient,
            @Value("${openai.api.model:gpt-3.5-turbo}") String model,
            @Value("${openai.api.temperature:0.0}") Double temperature,
            @Value("${openai.api.max-tokens:10}") Integer maxTokens) {
        this.openAIWebClient = openAIWebClient;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }
    
    /**
     * Analisa risco do pedido usando OpenAI.
     * 
     * <p>Anotações do Resilience4j:
     * - @CircuitBreaker: Nome "riskAnalysis" (configurado no application.properties)
     * - @Retry: Tenta novamente em falhas transitórias</p>
     * 
     * <p>Em caso de falha (circuito aberto, timeout, etc.),
     * retorna RiskAnalysisResult com status PENDING ao invés de lançar exceção.
     * Isso permite que o Use Case trate graciosamente.</p>
     */
    @Override
    @CircuitBreaker(name = "riskAnalysis", fallbackMethod = "analyzeRiskFallback")
    @Retry(name = "riskAnalysis")
    public RiskAnalysisResult analyzeRisk(RiskAnalysisRequest request) {
        log.info("Analyzing risk via OpenAI for order: {}", request.orderId());
        
        try {
            // Construir prompt estruturado
            String prompt = buildRiskAnalysisPrompt(request);
            
            // Criar requisição OpenAI
            OpenAIRequest openAIRequest = buildOpenAIRequest(prompt);
            
            // Chamar API do OpenAI
            OpenAIResponse response = openAIWebClient
                .post()
                .uri("/chat/completions")
                .bodyValue(openAIRequest)
                .retrieve()
                .bodyToMono(OpenAIResponse.class)
                .block(); // Bloqueia para retorno síncrono
            
            // Converter resposta para RiskAnalysisResult (domínio)
            return mapToRiskAnalysisResult(response, request);
            
        } catch (WebClientResponseException e) {
            log.error("OpenAI API error for order {}: {} - {}", 
                request.orderId(), e.getStatusCode(), e.getResponseBodyAsString());
            return createPendingResult(request, 
                String.format("OpenAI API error: %s", e.getStatusCode()));
        } catch (Exception e) {
            log.error("Unexpected error analyzing risk for order {}: {}", 
                request.orderId(), e.getMessage(), e);
            return createPendingResult(request, "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Fallback method chamado quando Circuit Breaker está aberto.
     * 
     * <p>Retorna RiskAnalysisResult com PENDING ao invés de lançar exceção.
     * Isso permite degradação graciosa quando OpenAI está indisponível.</p>
     */
    private RiskAnalysisResult analyzeRiskFallback(RiskAnalysisRequest request, Exception e) {
        log.warn("Circuit breaker open for OpenAI. Returning PENDING risk level. Order: {}", 
            request.orderId());
        return createPendingResult(request, 
            "Risk analysis temporarily unavailable (circuit breaker open)");
    }
    
    /**
     * Constrói prompt estruturado para análise de risco.
     * 
     * <p>O prompt é projetado para que a IA retorne:
     * <ul>
     *   <li>Na primeira linha: "LOW" ou "HIGH" (para classificação)</li>
     *   <li>Na(s) linha(s) seguinte(s): uma explicação curta do racional</li>
     * </ul>
     * Isso facilita o parsing do nível de risco e, ao mesmo tempo, preserva o
     * racional da IA para logging e auditoria.</p>
     */
    private String buildRiskAnalysisPrompt(RiskAnalysisRequest request) {
        return String.format(
            "You are a risk analysis system for e-commerce orders. " +
            "Analyze the following order and classify the risk.\n\n" +
            "Order Information:\n" +
            "- Order ID: %s\n" +
            "- Amount: %s\n" +
            "- Customer ID: %s\n" +
            "- Customer Email: %s\n" +
            "- Payment Method: %s\n" +
            "- Additional Context: %s\n\n" +
            "Return 'LOW' if the order appears safe and normal. " +
            "Return 'HIGH' if there are risk indicators such as:\n" +
            "- Unusually high value for new customers\n" +
            "- Suspicious payment patterns\n" +
            "- Unusual customer behavior\n" +
            "- Other fraud indicators\n\n" +
            "Response (LOW or HIGH only):",
            request.orderId(),
            request.orderAmount(),
            request.customerId(),
            request.customerEmail(),
            request.paymentMethod(),
            request.additionalContext()
        ) + "\n\n" +
            "Respond in the following format:\n" +
            "Line 1: ONLY the word LOW or HIGH (risk classification).\n" +
            "Line 2: A short explanation (max 2 sentences) of why you chose this classification.";
    }
    
    /**
     * Constrói OpenAIRequest a partir do prompt.
     */
    private OpenAIRequest buildOpenAIRequest(String prompt) {
        // Mensagem do sistema (instruções)
        OpenAIRequest.Message systemMessage = OpenAIRequest.Message.builder()
            .role("system")
            .content("You are a risk analysis assistant. Always respond with only 'LOW' or 'HIGH'.")
            .build();
        
        // Mensagem do usuário (dados do pedido)
        OpenAIRequest.Message userMessage = OpenAIRequest.Message.builder()
            .role("user")
            .content(prompt)
            .build();
        
        return OpenAIRequest.builder()
            .model(model)
            .messages(List.of(systemMessage, userMessage))
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();
    }
    
    /**
     * Converte resposta do OpenAI para RiskAnalysisResult do domínio.
     */
    private RiskAnalysisResult mapToRiskAnalysisResult(OpenAIResponse response, RiskAnalysisRequest request) {
        if (response == null || !response.isSuccess()) {
            log.warn("Invalid response from OpenAI for order: {}", request.orderId());
            return createPendingResult(request, "Invalid response from OpenAI");
        }
        
        String content = response.getContent();
        if (content == null || content.isBlank()) {
            log.warn("Empty response from OpenAI for order: {}", request.orderId());
            return createPendingResult(request, "Empty response from OpenAI");
        }
        
        // Parse da resposta: primeira linha deve conter "LOW" ou "HIGH",
        // linhas seguintes podem conter o racional da análise.
        RiskLevel riskLevel = parseRiskLevel(content);
        
        log.info("OpenAI risk analysis for order {}: {} (response: '{}')", 
            request.orderId(), riskLevel, content);
        
        return new RiskAnalysisResult(
            riskLevel,
            null, // confidenceScore (não disponível na resposta simples)
            content, // preserva response completa como racional da IA
            java.time.LocalDateTime.now()
        );
    }
    
    /**
     * Faz parse da resposta da IA para RiskLevel.
     * 
     * <p>Extrai "LOW" ou "HIGH" da resposta, ignorando espaços e case.
     * Se não conseguir parsear, retorna PENDING (fail-safe).</p>
     */
    private RiskLevel parseRiskLevel(String content) {
        String normalized = content.trim().toUpperCase();
        
        if (normalized.contains("LOW")) {
            return RiskLevel.LOW;
        } else if (normalized.contains("HIGH")) {
            return RiskLevel.HIGH;
        } else {
            log.warn("Could not parse risk level from OpenAI response: '{}'. Returning PENDING.", content);
            return RiskLevel.PENDING;
        }
    }
    
    /**
     * Cria RiskAnalysisResult com status PENDING (fallback).
     */
    private RiskAnalysisResult createPendingResult(RiskAnalysisRequest request, String reason) {
        return new RiskAnalysisResult(
            RiskLevel.PENDING,
            null, // confidenceScore (não disponível em fallback)
            reason,
            java.time.LocalDateTime.now()
        );
    }
}

