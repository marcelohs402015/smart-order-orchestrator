package com.marcelo.orchestrator.infrastructure.ai.adapter;

import com.marcelo.orchestrator.domain.model.RiskLevel;
import com.marcelo.orchestrator.domain.port.RiskAnalysisRequest;
import com.marcelo.orchestrator.domain.port.RiskAnalysisResult;
import com.marcelo.orchestrator.infrastructure.ai.dto.OpenAIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para OpenAIRiskAnalysisAdapter.
 * 
 * <p>Testa o adaptador que integra com OpenAI usando mocks do WebClient.
 * Foca em validar construção de prompts, parsing de respostas e tratamento de erros.</p>
 * 
 * <h3>Estratégia de Teste:</h3>
 * <ul>
 *   <li><strong>Mocks:</strong> WebClient é mockado para não fazer chamadas reais</li>
 *   <li><strong>Isolamento:</strong> Testa apenas lógica do adapter, não API real</li>
 *   <li><strong>Cenários:</strong> Sucesso (LOW/HIGH), falha de API, timeout, parsing inválido</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAIRiskAnalysisAdapter Tests")
class OpenAIRiskAnalysisAdapterTest {
    
    @Mock
    private WebClient openAIWebClient;
    
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    
    @Mock
    private WebClient.ResponseSpec responseSpec;
    
    @Mock
    private java.util.concurrent.Executor virtualThreadExecutor;
    
    private OpenAIRiskAnalysisAdapter adapter;
    
    private RiskAnalysisRequest testRequest;
    
    @BeforeEach
    void setUp() {
        adapter = new OpenAIRiskAnalysisAdapter(
            openAIWebClient,
            "gpt-3.5-turbo",
            0.0,
            10,
            virtualThreadExecutor
        );
        
        // Criar RiskAnalysisRequest de teste
        testRequest = new RiskAnalysisRequest(
            UUID.randomUUID(),
            BigDecimal.valueOf(100.50),
            UUID.randomUUID(),
            "cliente@teste.com",
            "PIX",
            "Order: ORD-123, Amount: 100.50, Customer: cliente@teste.com, Items: 2"
        );
    }
    
    @Test
    @DisplayName("Deve analisar risco e retornar LOW")
    void shouldAnalyzeRiskAndReturnLow() {
        // Arrange
        OpenAIResponse response = new OpenAIResponse();
        OpenAIResponse.Choice choice = new OpenAIResponse.Choice();
        OpenAIResponse.Message message = new OpenAIResponse.Message();
        message.setRole("assistant");
        message.setContent("LOW");
        choice.setMessage(message);
        response.setChoices(List.of(choice));
        
        // Mock WebClient chain
        when(openAIWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OpenAIResponse.class))
            .thenReturn(Mono.just(response));
        
        // Act
        RiskAnalysisResult result = adapter.analyzeRisk(testRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(RiskLevel.LOW, result.riskLevel());
        assertNotNull(result.reason());
    }
    
    @Test
    @DisplayName("Deve analisar risco e retornar HIGH")
    void shouldAnalyzeRiskAndReturnHigh() {
        // Arrange
        OpenAIResponse response = new OpenAIResponse();
        OpenAIResponse.Choice choice = new OpenAIResponse.Choice();
        OpenAIResponse.Message message = new OpenAIResponse.Message();
        message.setRole("assistant");
        message.setContent("HIGH");
        choice.setMessage(message);
        response.setChoices(List.of(choice));
        
        // Mock WebClient chain
        when(openAIWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OpenAIResponse.class))
            .thenReturn(Mono.just(response));
        
        // Act
        RiskAnalysisResult result = adapter.analyzeRisk(testRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(RiskLevel.HIGH, result.riskLevel());
    }
    
    @Test
    @DisplayName("Deve tratar erro da API do OpenAI graciosamente")
    void shouldHandleApiErrorGracefully() {
        // Arrange
        WebClientResponseException exception = mock(WebClientResponseException.class);
        when(exception.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.UNAUTHORIZED);
        when(exception.getResponseBodyAsString()).thenReturn("Invalid API key");
        
        when(openAIWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OpenAIResponse.class))
            .thenReturn(Mono.error(exception));
        
        // Act
        RiskAnalysisResult result = adapter.analyzeRisk(testRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(RiskLevel.PENDING, result.riskLevel());
        assertNotNull(result.reason());
        assertTrue(result.reason().contains("OpenAI API error"));
    }
    
    @Test
    @DisplayName("Deve retornar PENDING quando resposta não contém LOW ou HIGH")
    void shouldReturnPendingWhenResponseIsInvalid() {
        // Arrange
        OpenAIResponse response = new OpenAIResponse();
        OpenAIResponse.Choice choice = new OpenAIResponse.Choice();
        OpenAIResponse.Message message = new OpenAIResponse.Message();
        message.setRole("assistant");
        message.setContent("INVALID_RESPONSE");
        choice.setMessage(message);
        response.setChoices(List.of(choice));
        
        // Mock WebClient chain
        when(openAIWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OpenAIResponse.class))
            .thenReturn(Mono.just(response));
        
        // Act
        RiskAnalysisResult result = adapter.analyzeRisk(testRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(RiskLevel.PENDING, result.riskLevel());
    }
}

