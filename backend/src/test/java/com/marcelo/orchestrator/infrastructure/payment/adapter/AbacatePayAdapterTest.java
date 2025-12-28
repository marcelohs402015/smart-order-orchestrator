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
    
    private java.util.concurrent.Executor virtualThreadExecutor;
    
    private AbacatePayAdapter adapter;
    
    private PaymentRequest testPaymentRequest;
    
    @BeforeEach
    void setUp() {
        virtualThreadExecutor = Runnable::run;
        
        adapter = new AbacatePayAdapter(
            abacatePayWebClient,
            virtualThreadExecutor
        );
        
        
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
        
        AbacatePayBillingResponse.AbacatePayBillingData data = new AbacatePayBillingResponse.AbacatePayBillingData(
            "bill_123456",
            "https://example.com/payment",
            10050,
            "PAID",
            true,
            new String[]{"PIX"},
            "ONE_TIME",
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        AbacatePayBillingResponse response = new AbacatePayBillingResponse(
            data,   
            null    
        );
        
        
        when(abacatePayWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AbacatePayBillingResponse.class))
            .thenReturn(Mono.just(response));
        
        
        PaymentResult result = adapter.processPayment(testPaymentRequest);
        
        
        assertNotNull(result);
        assertEquals("bill_123456", result.paymentId());
        assertEquals(PaymentStatus.SUCCESS, result.status());
        assertTrue(result.isSuccessful());
    }
    
    @Test
    @DisplayName("Deve tratar erro da API do AbacatePay graciosamente")
    void shouldHandleApiErrorGracefully() {
        RuntimeException exception = new RuntimeException("API Error");
        
        when(abacatePayWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AbacatePayBillingResponse.class))
            .thenReturn(Mono.error(exception));
        
        PaymentResult result = adapter.processPayment(testPaymentRequest);
        
        assertNotNull(result);
        assertTrue(result.isFailed());
        assertEquals(PaymentStatus.FAILED, result.status());
        assertNotNull(result.message());
    }
}

