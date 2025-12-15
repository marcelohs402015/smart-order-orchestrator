package com.marcelo.orchestrator.infrastructure.payment.adapter;

import com.marcelo.orchestrator.domain.port.PaymentRequest;
import com.marcelo.orchestrator.domain.port.PaymentResult;
import com.marcelo.orchestrator.domain.port.PaymentStatus;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayBillingResponse;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AbacatePayAdapter.
 * 
 * <p>Testa o adaptador que integra com AbacatePay usando mocks do WebClient.
 * Foca em validar conversão de dados e tratamento de erros.</p>
 * 
 * <h3>Estratégia de Teste:</h3>
 * <ul>
 *   <li><strong>Mocks:</strong> WebClient é mockado para não fazer chamadas reais</li>
 *   <li><strong>Isolamento:</strong> Testa apenas lógica do adapter, não API real</li>
 *   <li><strong>Cenários:</strong> Sucesso, falha de API, timeout, etc.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AbacatePayAdapter Tests")
class AbacatePayAdapterTest {
    
    @Mock
    private WebClient abacatePayWebClient;
    
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    
    @Mock
    private WebClient.ResponseSpec responseSpec;
    
    @InjectMocks
    private AbacatePayAdapter adapter;
    
    private PaymentRequest testPaymentRequest;
    
    @BeforeEach
    void setUp() {
        // Configurar base URL via reflection (já que é @Value)
        ReflectionTestUtils.setField(adapter, "baseUrl", "https://api.abacatepay.com/v1");
        
        // Criar PaymentRequest de teste
        testPaymentRequest = new PaymentRequest(
            UUID.randomUUID(),
            BigDecimal.valueOf(100.50),
            "BRL",
            "PIX",
            "cliente@teste.com"
        );
    }
    
    @Test
    @DisplayName("Deve processar pagamento com sucesso")
    void shouldProcessPaymentSuccessfully() {
        // Arrange - Criar Records com construtores (Records são imutáveis)
        AbacatePayBillingResponse.AbacatePayBillingData data = new AbacatePayBillingResponse.AbacatePayBillingData(
            "bill_123456",                    // id
            "https://abacatepay.com/pay/123", // url
            10050,                            // amount (em centavos)
            "PAID",                           // status
            true,                             // devMode
            new String[]{"PIX"},             // methods
            "ONE_TIME",                       // frequency
            null,                             // customer (não necessário para este teste)
            LocalDateTime.now(),              // createdAt
            LocalDateTime.now()               // updatedAt
        );
        
        AbacatePayBillingResponse response = new AbacatePayBillingResponse(
            data,   // data
            null    // error
        );
        
        // Mock WebClient chain
        when(abacatePayWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AbacatePayBillingResponse.class))
            .thenReturn(Mono.just(response));
        
        // Act
        PaymentResult result = adapter.processPayment(testPaymentRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("bill_123456", result.paymentId());
        assertEquals(PaymentStatus.SUCCESS, result.status());
        assertTrue(result.isSuccessful());
    }
    
    @Test
    @DisplayName("Deve tratar erro da API do AbacatePay graciosamente")
    void shouldHandleApiErrorGracefully() {
        // Arrange
        WebClientResponseException exception = mock(WebClientResponseException.class);
        when(exception.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsString()).thenReturn("Invalid request");
        
        when(abacatePayWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AbacatePayBillingResponse.class))
            .thenReturn(Mono.error(exception));
        
        // Act
        PaymentResult result = adapter.processPayment(testPaymentRequest);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isFailed());
        assertEquals(PaymentStatus.FAILED, result.status());
        assertNotNull(result.message());
    }
}

