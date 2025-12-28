package com.marcelo.orchestrator.infrastructure.ai.adapter;

import com.marcelo.orchestrator.domain.model.RiskLevel;
import com.marcelo.orchestrator.domain.port.RiskAnalysisPort;
import com.marcelo.orchestrator.domain.port.RiskAnalysisRequest;
import com.marcelo.orchestrator.domain.port.RiskAnalysisResult;
import com.marcelo.orchestrator.infrastructure.ai.dto.OpenAIRequest;
import com.marcelo.orchestrator.infrastructure.ai.dto.OpenAIResponse;
import com.marcelo.orchestrator.infrastructure.aspect.Loggable;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Loggable
@Component
public class OpenAIRiskAnalysisAdapter implements RiskAnalysisPort {
    
    private final WebClient openAIWebClient;
    private final String model;
    private final Double temperature;
    private final Integer maxTokens;
    private final Executor virtualThreadExecutor;
    
    public OpenAIRiskAnalysisAdapter(
            WebClient openAIWebClient,
            @Value("${openai.api.model:gpt-3.5-turbo}") String model,
            @Value("${openai.api.temperature:0.0}") Double temperature,
            @Value("${openai.api.max-tokens:10}") Integer maxTokens,
            @Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor) {
        this.openAIWebClient = openAIWebClient;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }
    
    @Override
    @CircuitBreaker(name = "riskAnalysis", fallbackMethod = "analyzeRiskFallback")
    @Retry(name = "riskAnalysis")
    public RiskAnalysisResult analyzeRisk(RiskAnalysisRequest request) {
        String prompt = buildRiskAnalysisPrompt(request);
        OpenAIRequest openAIRequest = buildOpenAIRequest(prompt);
        
        CompletableFuture<OpenAIResponse> future = CompletableFuture.supplyAsync(() ->
            openAIWebClient
                .post()
                .uri("/chat/completions")
                .bodyValue(openAIRequest)
                .retrieve()
                .bodyToMono(OpenAIResponse.class)
                .block(), virtualThreadExecutor);
        
        try {
            OpenAIResponse response = future.join();
            return mapToRiskAnalysisResult(response, request);
        } catch (Exception e) {
            return createPendingResult(request, "Error: " + e.getMessage());
        }
    }
    
    private RiskAnalysisResult analyzeRiskFallback(RiskAnalysisRequest request, Exception e) {
        return createPendingResult(request, 
            "Risk analysis temporarily unavailable (circuit breaker open)");
    }
    
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
    
    private OpenAIRequest buildOpenAIRequest(String prompt) {
        OpenAIRequest.Message systemMessage = OpenAIRequest.Message.builder()
            .role("system")
            .content("You are a risk analysis assistant. Always respond with only 'LOW' or 'HIGH'.")
            .build();
        
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
    
    private RiskAnalysisResult mapToRiskAnalysisResult(OpenAIResponse response, RiskAnalysisRequest request) {
        if (response == null || !response.isSuccess()) {
            return createPendingResult(request, "Invalid response from OpenAI");
        }
        
        String content = response.getContent();
        if (content == null || content.isBlank()) {
            return createPendingResult(request, "Empty response from OpenAI");
        }
        
        RiskLevel riskLevel = parseRiskLevel(content);
        
        return new RiskAnalysisResult(
            riskLevel,
            null,
            content,
            java.time.LocalDateTime.now()
        );
    }
    
    private RiskLevel parseRiskLevel(String content) {
        String normalized = content.trim().toUpperCase();
        
        if (normalized.contains("LOW")) {
            return RiskLevel.LOW;
        } else if (normalized.contains("HIGH")) {
            return RiskLevel.HIGH;
        } else {
            return RiskLevel.PENDING;
        }
    }
    
    private RiskAnalysisResult createPendingResult(@SuppressWarnings("unused") RiskAnalysisRequest request, String reason) {
        return new RiskAnalysisResult(
            RiskLevel.PENDING,
            null, 
            reason,
            java.time.LocalDateTime.now()
        );
    }
}

