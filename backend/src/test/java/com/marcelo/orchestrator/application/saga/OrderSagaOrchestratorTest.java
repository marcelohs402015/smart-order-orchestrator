package com.marcelo.orchestrator.application.saga;

import com.marcelo.orchestrator.application.usecase.AnalyzeRiskUseCase;
import com.marcelo.orchestrator.application.usecase.CreateOrderUseCase;
import com.marcelo.orchestrator.application.usecase.ProcessPaymentUseCase;
import com.marcelo.orchestrator.domain.model.*;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.infrastructure.persistence.entity.SagaExecutionEntity;
import com.marcelo.orchestrator.infrastructure.persistence.repository.JpaSagaExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para OrderSagaOrchestrator.
 * 
 * <p>Testa a orquestração completa da saga, incluindo:
 * - Execução sequencial dos 3 passos
 * - Rastreamento de estado
 * - Compensação em caso de falha
 * - Persistência de histórico</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderSagaOrchestrator Tests")
class OrderSagaOrchestratorTest {
    
    @Mock
    private CreateOrderUseCase createOrderUseCase;
    
    @Mock
    private ProcessPaymentUseCase processPaymentUseCase;
    
    @Mock
    private AnalyzeRiskUseCase analyzeRiskUseCase;
    
    @Mock
    private OrderRepositoryPort orderRepository;
    
    @Mock
    private JpaSagaExecutionRepository sagaRepository;
    
    @Mock
    private EventPublisherPort eventPublisher;
    
    @InjectMocks
    private OrderSagaOrchestrator orchestrator;
    
    private OrderSagaCommand command;
    private Order createdOrder;
    private Order paidOrder;
    private Order analyzedOrder;
    
    @BeforeEach
    void setUp() {
        // Criar command
        command = OrderSagaCommand.builder()
            .customerId(UUID.randomUUID())
            .customerName("Cliente Teste")
            .customerEmail("cliente@teste.com")
            .items(List.of(
                OrderItem.builder()
                    .productId(UUID.randomUUID())
                    .productName("Produto 1")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(50.00))
                    .build()
            ))
            .paymentMethod("PIX")
            .currency("BRL")
            .build();
        
        // Criar pedido criado
        createdOrder = Order.builder()
            .id(UUID.randomUUID())
            .orderNumber("ORD-123")
            .status(OrderStatus.PENDING)
            .customerId(command.getCustomerId())
            .customerName(command.getCustomerName())
            .customerEmail(command.getCustomerEmail())
            .items(command.getItems())
            .totalAmount(BigDecimal.valueOf(100.00))
            .riskLevel(RiskLevel.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // Criar pedido pago
        paidOrder = Order.builder()
            .id(createdOrder.getId())
            .orderNumber(createdOrder.getOrderNumber())
            .status(OrderStatus.PAID)
            .customerId(createdOrder.getCustomerId())
            .customerName(createdOrder.getCustomerName())
            .customerEmail(createdOrder.getCustomerEmail())
            .items(createdOrder.getItems())
            .totalAmount(createdOrder.getTotalAmount())
            .paymentId("PAY-123")
            .riskLevel(RiskLevel.PENDING)
            .createdAt(createdOrder.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // Criar pedido analisado
        analyzedOrder = Order.builder()
            .id(paidOrder.getId())
            .orderNumber(paidOrder.getOrderNumber())
            .status(paidOrder.getStatus())
            .customerId(paidOrder.getCustomerId())
            .customerName(paidOrder.getCustomerName())
            .customerEmail(paidOrder.getCustomerEmail())
            .items(paidOrder.getItems())
            .totalAmount(paidOrder.getTotalAmount())
            .paymentId(paidOrder.getPaymentId())
            .riskLevel(RiskLevel.LOW)
            .createdAt(paidOrder.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // Mock saga repository
        when(sagaRepository.save(any(SagaExecutionEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }
    
    @Test
    @DisplayName("Deve executar saga completa com sucesso")
    void shouldExecuteCompleteSagaSuccessfully() {
        // Arrange
        when(createOrderUseCase.execute(any())).thenReturn(createdOrder);
        when(processPaymentUseCase.execute(any())).thenReturn(paidOrder);
        when(analyzeRiskUseCase.execute(any())).thenReturn(analyzedOrder);
        
        // Act
        OrderSagaResult result = orchestrator.execute(command);
        
        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getOrder());
        assertEquals(RiskLevel.LOW, result.getOrder().getRiskLevel());
        assertNotNull(result.getSagaExecutionId());
        
        // Verificar que todos os passos foram executados
        verify(createOrderUseCase, times(1)).execute(any());
        verify(processPaymentUseCase, times(1)).execute(any());
        verify(analyzeRiskUseCase, times(1)).execute(any());
        
        // Verificar que saga foi persistida
        ArgumentCaptor<SagaExecutionEntity> sagaCaptor = ArgumentCaptor.forClass(SagaExecutionEntity.class);
        verify(sagaRepository, atLeast(3)).save(sagaCaptor.capture());
    }
    
    @Test
    @DisplayName("Deve compensar quando pagamento falhar mantendo status PAYMENT_FAILED")
    void shouldCompensateWhenPaymentFails() {
        // Arrange
        Order paymentFailedOrder = Order.builder()
            .id(createdOrder.getId())
            .orderNumber(createdOrder.getOrderNumber())
            .status(OrderStatus.PAYMENT_FAILED)
            .customerId(createdOrder.getCustomerId())
            .customerName(createdOrder.getCustomerName())
            .customerEmail(createdOrder.getCustomerEmail())
            .items(createdOrder.getItems())
            .totalAmount(createdOrder.getTotalAmount())
            .riskLevel(RiskLevel.PENDING)
            .createdAt(createdOrder.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(createOrderUseCase.execute(any())).thenReturn(createdOrder);
        when(processPaymentUseCase.execute(any())).thenReturn(paymentFailedOrder);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        OrderSagaResult result = orchestrator.execute(command);
        
        // Assert
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertEquals("Payment failed", result.getErrorMessage());
        
        // Verificar que pedido mantém status PAYMENT_FAILED (não muda para CANCELED)
        // Isso permite que o frontend identifique corretamente a causa da falha
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.PAYMENT_FAILED, savedOrder.getStatus());
        
        // Verificar que análise de risco não foi executada
        verify(analyzeRiskUseCase, never()).execute(any());
    }
    
    @Test
    @DisplayName("Deve rastrear todos os passos da saga")
    void shouldTrackAllSagaSteps() {
        // Arrange
        when(createOrderUseCase.execute(any())).thenReturn(createdOrder);
        when(processPaymentUseCase.execute(any())).thenReturn(paidOrder);
        when(analyzeRiskUseCase.execute(any())).thenReturn(analyzedOrder);
        
        // Act
        orchestrator.execute(command);
        
        // Assert
        ArgumentCaptor<SagaExecutionEntity> sagaCaptor = ArgumentCaptor.forClass(SagaExecutionEntity.class);
        verify(sagaRepository, atLeast(3)).save(sagaCaptor.capture());
        
        // Verificar que saga tem os 3 passos
        List<SagaExecutionEntity> savedSagas = sagaCaptor.getAllValues();
        SagaExecutionEntity finalSaga = savedSagas.get(savedSagas.size() - 1);
        
        assertNotNull(finalSaga.getSteps());
        assertEquals(3, finalSaga.getSteps().size());
        assertEquals(SagaExecutionEntity.SagaStatus.COMPLETED, finalSaga.getStatus());
    }
}

