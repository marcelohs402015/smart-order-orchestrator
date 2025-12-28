package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.application.exception.OrderNotFoundException;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.domain.model.RiskLevel;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.domain.port.RiskAnalysisPort;
import com.marcelo.orchestrator.domain.port.RiskAnalysisRequest;
import com.marcelo.orchestrator.domain.port.RiskAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyzeRiskUseCase Tests")
class AnalyzeRiskUseCaseTest {
    
    @Mock
    private OrderRepositoryPort orderRepository;
    
    @Mock
    private RiskAnalysisPort riskAnalysisPort;
    
    @InjectMocks
    private AnalyzeRiskUseCase useCase;
    
    private Order paidOrder;
    private AnalyzeRiskCommand command;
    
    @BeforeEach
    void setUp() {
        OrderItem item = OrderItem.builder()
            .productId(UUID.randomUUID())
            .productName("Produto 1")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(50.00))
            .build();
        
        paidOrder = Order.builder()
            .id(UUID.randomUUID())
            .orderNumber("ORD-1234567890")
            .status(OrderStatus.PAID)
            .customerId(UUID.randomUUID())
            .customerName("Cliente Teste")
            .customerEmail("cliente@teste.com")
            .items(List.of(item))
            .totalAmount(BigDecimal.valueOf(100.00))
            .riskLevel(RiskLevel.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        paidOrder.calculateTotal();
        
        command = AnalyzeRiskCommand.builder()
            .orderId(paidOrder.getId())
            .paymentMethod("PIX")
            .build();
        
        ReflectionTestUtils.setField(useCase, "riskAnalysisEnabled", true);
    }
    
    @Test
    @DisplayName("Deve analisar risco com sucesso e retornar LOW")
    void shouldAnalyzeRiskSuccessfullyAndReturnLow() {
        RiskAnalysisResult result = new RiskAnalysisResult(
            RiskLevel.LOW,
            null,
            "Risk analysis completed: LOW",
            LocalDateTime.now()
        );
        
        when(orderRepository.findById(command.getOrderId()))
            .thenReturn(Optional.of(paidOrder));
        when(riskAnalysisPort.analyzeRisk(any(RiskAnalysisRequest.class)))
            .thenReturn(result);
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        Order updatedOrder = useCase.execute(command);
        
        assertNotNull(updatedOrder);
        assertEquals(RiskLevel.LOW, updatedOrder.getRiskLevel());
        verify(riskAnalysisPort, times(1)).analyzeRisk(any(RiskAnalysisRequest.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    
    @Test
    @DisplayName("Deve analisar risco com sucesso e retornar HIGH")
    void shouldAnalyzeRiskSuccessfullyAndReturnHigh() {
        RiskAnalysisResult result = new RiskAnalysisResult(
            RiskLevel.HIGH,
            null,
            "Risk analysis completed: HIGH",
            LocalDateTime.now()
        );
        
        when(orderRepository.findById(command.getOrderId()))
            .thenReturn(Optional.of(paidOrder));
        when(riskAnalysisPort.analyzeRisk(any(RiskAnalysisRequest.class)))
            .thenReturn(result);
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        Order updatedOrder = useCase.execute(command);
        
        assertNotNull(updatedOrder);
        assertEquals(RiskLevel.HIGH, updatedOrder.getRiskLevel());
        verify(riskAnalysisPort, times(1)).analyzeRisk(any(RiskAnalysisRequest.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando pedido não encontrado")
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById(command.getOrderId()))
            .thenReturn(Optional.empty());
        
        OrderNotFoundException exception = assertThrows(
            OrderNotFoundException.class,
            () -> useCase.execute(command)
        );
        
        assertTrue(exception.getMessage().contains("Order not found"));
        verify(riskAnalysisPort, never()).analyzeRisk(any());
        verify(orderRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando pedido não está PAID")
    void shouldThrowExceptionWhenOrderNotPaid() {
        Order pendingOrder = Order.builder()
            .id(paidOrder.getId())
            .orderNumber(paidOrder.getOrderNumber())
            .status(OrderStatus.PENDING)
            .customerId(paidOrder.getCustomerId())
            .customerName(paidOrder.getCustomerName())
            .customerEmail(paidOrder.getCustomerEmail())
            .items(paidOrder.getItems())
            .totalAmount(paidOrder.getTotalAmount())
            .riskLevel(RiskLevel.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(orderRepository.findById(command.getOrderId()))
            .thenReturn(Optional.of(pendingOrder));
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> useCase.execute(command)
        );
        
        assertTrue(exception.getMessage().contains("Order must be PAID"));
        verify(riskAnalysisPort, never()).analyzeRisk(any());
        verify(orderRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Deve manter PENDING quando análise de risco falha (fallback gracioso)")
    void shouldKeepPendingWhenRiskAnalysisFails() {
        when(orderRepository.findById(command.getOrderId()))
            .thenReturn(Optional.of(paidOrder));
        when(riskAnalysisPort.analyzeRisk(any(RiskAnalysisRequest.class)))
            .thenThrow(new RuntimeException("OpenAI API unavailable"));
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        Order updatedOrder = useCase.execute(command);
        
        assertNotNull(updatedOrder);
        assertEquals(RiskLevel.PENDING, updatedOrder.getRiskLevel());
        verify(riskAnalysisPort, times(1)).analyzeRisk(any(RiskAnalysisRequest.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    
    @Test
    @DisplayName("Deve construir RiskAnalysisRequest corretamente")
    void shouldBuildRiskAnalysisRequestCorrectly() {
        RiskAnalysisResult result = new RiskAnalysisResult(
            RiskLevel.LOW,
            null,
            "Risk analysis completed",
            LocalDateTime.now()
        );
        
        when(orderRepository.findById(command.getOrderId()))
            .thenReturn(Optional.of(paidOrder));
        when(riskAnalysisPort.analyzeRisk(any(RiskAnalysisRequest.class)))
            .thenReturn(result);
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        useCase.execute(command);
        
        verify(riskAnalysisPort, times(1)).analyzeRisk(argThat(request ->
            request.orderId().equals(paidOrder.getId()) &&
            request.orderAmount().equals(paidOrder.getTotalAmount()) &&
            request.customerId().equals(paidOrder.getCustomerId()) &&
            request.customerEmail().equals(paidOrder.getCustomerEmail()) &&
            request.paymentMethod().equals("PIX") &&
            request.additionalContext() != null
        ));
    }
}

