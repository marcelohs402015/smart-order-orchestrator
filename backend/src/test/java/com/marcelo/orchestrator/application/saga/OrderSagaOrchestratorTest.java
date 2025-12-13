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
        
        // Mock saga repository - save retorna o argumento (simula persistência)
        when(sagaRepository.save(any(SagaExecutionRepositoryPort.SagaExecution.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock findById para retornar empty por padrão (pode ser sobrescrito em testes específicos)
        lenient().when(sagaRepository.findById(any(UUID.class)))
            .thenReturn(Optional.empty());
        
        // Mock findByIdempotencyKey - não mockar aqui para evitar UnnecessaryStubbing
        // Será mockado apenas nos testes que precisam
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
        ArgumentCaptor<SagaExecutionRepositoryPort.SagaExecution> sagaCaptor = 
            ArgumentCaptor.forClass(SagaExecutionRepositoryPort.SagaExecution.class);
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
        ArgumentCaptor<SagaExecutionRepositoryPort.SagaExecution> sagaCaptor = 
            ArgumentCaptor.forClass(SagaExecutionRepositoryPort.SagaExecution.class);
        
        // Verificar que saga foi salva múltiplas vezes:
        // 1. Início da saga (startSagaSafely)
        // 2. Step 1 STARTED (startStep)
        // 3. Step 1 COMPLETED (completeStep)
        // 4. Step 2 STARTED (startStep)
        // 5. Step 2 COMPLETED (completeStep)
        // 6. Step 3 STARTED (startStep)
        // 7. Step 3 COMPLETED (completeStep)
        // 8. Saga COMPLETED (completeSaga)
        // Total: pelo menos 8 saves (mas pode ser mais devido a atualizações de status)
        verify(sagaRepository, atLeast(6)).save(sagaCaptor.capture());
        
        // Verificar que saga foi salva múltiplas vezes (uma vez por step + finalização)
        List<SagaExecutionRepositoryPort.SagaExecution> savedSagas = sagaCaptor.getAllValues();
        assertFalse(savedSagas.isEmpty(), "Saga deve ser salva pelo menos uma vez");
        
        // Pegar a última saga salva (deve ter status COMPLETED)
        SagaExecutionRepositoryPort.SagaExecution finalSaga = savedSagas.get(savedSagas.size() - 1);
        
        // Verificar status final
        assertEquals(SagaExecutionRepositoryPort.SagaExecution.SagaStatus.COMPLETED, finalSaga.status());
        
        // Verificar que múltiplas sagas foram salvas (indica que steps foram rastreados)
        // Cada step adiciona/atualiza a saga, então devemos ter pelo menos 6 saves
        assertTrue(savedSagas.size() >= 6, 
            "Saga deve ser salva múltiplas vezes para rastrear steps (esperado: >= 6, atual: " + savedSagas.size() + ")");
    }
}

