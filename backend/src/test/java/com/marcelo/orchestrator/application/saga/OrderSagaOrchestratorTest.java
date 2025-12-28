package com.marcelo.orchestrator.application.saga;

import com.marcelo.orchestrator.application.usecase.AnalyzeRiskUseCase;
import com.marcelo.orchestrator.application.usecase.CreateOrderUseCase;
import com.marcelo.orchestrator.application.usecase.ProcessPaymentUseCase;
import com.marcelo.orchestrator.domain.model.*;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.domain.port.SagaExecutionRepositoryPort;
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
    private SagaExecutionRepositoryPort sagaRepository;
    
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
        
        
        when(sagaRepository.save(any(SagaExecutionRepositoryPort.SagaExecution.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        
        lenient().when(sagaRepository.findById(any(UUID.class)))
            .thenReturn(Optional.empty());
        
        
        
    }
    
    @Test
    @DisplayName("Deve executar saga completa com sucesso")
    void shouldExecuteCompleteSagaSuccessfully() {
        
        when(createOrderUseCase.execute(any())).thenReturn(createdOrder);
        when(processPaymentUseCase.execute(any())).thenReturn(paidOrder);
        when(analyzeRiskUseCase.execute(any())).thenReturn(analyzedOrder);
        
        
        OrderSagaResult result = orchestrator.execute(command);
        
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getOrder());
        assertEquals(RiskLevel.LOW, result.getOrder().getRiskLevel());
        assertNotNull(result.getSagaExecutionId());
        
        
        verify(createOrderUseCase, times(1)).execute(any());
        verify(processPaymentUseCase, times(1)).execute(any());
        verify(analyzeRiskUseCase, times(1)).execute(any());
        
        
        ArgumentCaptor<SagaExecutionRepositoryPort.SagaExecution> sagaCaptor = 
            ArgumentCaptor.forClass(SagaExecutionRepositoryPort.SagaExecution.class);
        verify(sagaRepository, atLeast(3)).save(sagaCaptor.capture());
    }
    
    @Test
    @DisplayName("Deve compensar quando pagamento falhar mantendo status PAYMENT_FAILED")
    void shouldCompensateWhenPaymentFails() {
        
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
        
        
        OrderSagaResult result = orchestrator.execute(command);
        
        
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertEquals("Payment failed", result.getErrorMessage());
        
        
        
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.PAYMENT_FAILED, savedOrder.getStatus());
        
        
        verify(analyzeRiskUseCase, never()).execute(any());
    }
    
    @Test
    @DisplayName("Deve rastrear todos os passos da saga")
    void shouldTrackAllSagaSteps() {
        
        when(createOrderUseCase.execute(any())).thenReturn(createdOrder);
        when(processPaymentUseCase.execute(any())).thenReturn(paidOrder);
        when(analyzeRiskUseCase.execute(any())).thenReturn(analyzedOrder);
        
        
        orchestrator.execute(command);
        
        
        ArgumentCaptor<SagaExecutionRepositoryPort.SagaExecution> sagaCaptor = 
            ArgumentCaptor.forClass(SagaExecutionRepositoryPort.SagaExecution.class);
        
        
        
        
        
        
        
        
        
        
        
        verify(sagaRepository, atLeast(6)).save(sagaCaptor.capture());
        
        
        List<SagaExecutionRepositoryPort.SagaExecution> savedSagas = sagaCaptor.getAllValues();
        assertFalse(savedSagas.isEmpty(), "Saga deve ser salva pelo menos uma vez");
        
        
        SagaExecutionRepositoryPort.SagaExecution finalSaga = savedSagas.get(savedSagas.size() - 1);
        
        
        assertEquals(SagaExecutionRepositoryPort.SagaExecution.SagaStatus.COMPLETED, finalSaga.status());
        
        
        
        assertTrue(savedSagas.size() >= 6, 
            "Saga deve ser salva múltiplas vezes para rastrear steps (esperado: >= 6, atual: " + savedSagas.size() + ")");
    }
}

