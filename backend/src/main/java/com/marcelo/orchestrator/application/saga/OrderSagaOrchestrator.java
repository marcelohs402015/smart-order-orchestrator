package com.marcelo.orchestrator.application.saga;

import com.marcelo.orchestrator.application.usecase.AnalyzeRiskUseCase;
import com.marcelo.orchestrator.application.usecase.CreateOrderUseCase;
import com.marcelo.orchestrator.application.usecase.ProcessPaymentUseCase;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.infrastructure.persistence.entity.SagaExecutionEntity;
import com.marcelo.orchestrator.infrastructure.persistence.entity.SagaStepEntity;
import com.marcelo.orchestrator.infrastructure.persistence.repository.JpaSagaExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orquestrador da saga de pedidos.
 * 
 * <p>Implementa o padrão <strong>Saga Pattern (Orchestration)</strong> para
 * coordenar múltiplas operações em uma transação distribuída.</p>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Orquestrar os 3 passos da saga (Criar Pedido → Processar Pagamento → Analisar Risco)</li>
 *   <li>Rastrear estado de cada passo para observabilidade</li>
 *   <li>Executar compensação automática em caso de falha</li>
 *   <li>Persistir histórico completo de execução</li>
 * </ul>
 * 
 * <h3>Por que Saga Pattern?</h3>
 * <ul>
 *   <li><strong>Consistência Eventual:</strong> Garante que todas as operações sejam executadas na ordem correta</li>
 *   <li><strong>Compensação:</strong> Rollback automático se algum passo falhar</li>
 *   <li><strong>Observabilidade:</strong> Rastreamento completo de cada execução</li>
 *   <li><strong>Padrão Enterprise:</strong> Usado em microserviços e sistemas distribuídos</li>
 * </ul>
 * 
 * <h3>Fluxo da Saga:</h3>
 * <ol>
 *   <li><strong>Step 1:</strong> Criar pedido (status: PENDING)</li>
 *   <li><strong>Step 2:</strong> Processar pagamento (status: PAID ou PAYMENT_FAILED)</li>
 *   <li><strong>Step 3:</strong> Analisar risco (apenas se pagamento sucedeu)</li>
 *   <li><strong>Compensação:</strong> Se pagamento falhar, cancelar pedido</li>
 * </ol>
 * 
 * <h3>Observabilidade:</h3>
 * <p>Cada passo é rastreado e persistido, permitindo:
 * - Consultar histórico completo de execuções
 * - Identificar onde falhou
 * - Calcular métricas (taxa de sucesso, tempo médio)
 * - Debugging em produção</p>
 * 
 * @author Marcelo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaOrchestrator {
    
    private final CreateOrderUseCase createOrderUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final AnalyzeRiskUseCase analyzeRiskUseCase;
    private final OrderRepositoryPort orderRepository;
    private final JpaSagaExecutionRepository sagaRepository;
    
    /**
     * Executa a saga completa de criação de pedido.
     * 
     * <p>Orquestra os 3 passos sequencialmente, rastreando cada um
     * e executando compensação se necessário.</p>
     * 
     * @param command Command com todos os dados necessários
     * @return Resultado da saga (sucesso ou falha)
     */
    @Transactional
    public OrderSagaResult execute(OrderSagaCommand command) {
        // Iniciar rastreamento da saga
        SagaExecutionEntity saga = startSaga();
        log.info("Starting order saga: {}", saga.getId());
        
        try {
            // Step 1: Criar pedido
            Order order = executeStep1_CreateOrder(command, saga);
            
            // Step 2: Processar pagamento
            Order paidOrder = executeStep2_ProcessPayment(command, order, saga);
            
            // Step 3: Analisar risco (apenas se pagamento sucedeu)
            if (paidOrder.isPaid()) {
                Order analyzedOrder = executeStep3_AnalyzeRisk(command, paidOrder, saga);
                completeSaga(saga, analyzedOrder);
                return OrderSagaResult.success(analyzedOrder, saga.getId());
            } else {
                // Compensação: cancelar pedido se pagamento falhou
                compensate(saga, paidOrder, "Payment failed");
                return OrderSagaResult.failed(paidOrder, saga.getId(), "Payment failed");
            }
            
        } catch (Exception e) {
            log.error("Saga execution failed: {}", e.getMessage(), e);
            compensate(saga, null, e.getMessage());
            return OrderSagaResult.failed(null, saga.getId(), e.getMessage());
        }
    }
    
    /**
     * Step 1: Criar pedido.
     */
    private Order executeStep1_CreateOrder(OrderSagaCommand command, SagaExecutionEntity saga) {
        SagaStepEntity step = startStep(saga, "ORDER_CREATED");
        
        try {
            Order order = createOrderUseCase.execute(command.toCreateOrderCommand());
            completeStep(step, true, null);
            saga.setOrderId(order.getId());
            saga.setStatus(SagaExecutionEntity.SagaStatus.ORDER_CREATED);
            sagaRepository.save(saga);
            
            log.info("Step 1 completed: Order created - {}", order.getId());
            return order;
            
        } catch (Exception e) {
            completeStep(step, false, e.getMessage());
            throw new RuntimeException("Failed to create order", e);
        }
    }
    
    /**
     * Step 2: Processar pagamento.
     */
    private Order executeStep2_ProcessPayment(OrderSagaCommand command, Order order, SagaExecutionEntity saga) {
        SagaStepEntity step = startStep(saga, "PAYMENT_PROCESSED");
        
        try {
            Order paidOrder = processPaymentUseCase.execute(command.toProcessPaymentCommand(order.getId()));
            completeStep(step, paidOrder.isPaid(), paidOrder.isPaid() ? null : "Payment failed");
            saga.setStatus(SagaExecutionEntity.SagaStatus.PAYMENT_PROCESSED);
            sagaRepository.save(saga);
            
            log.info("Step 2 completed: Payment processed - Status: {}", paidOrder.getStatus());
            return paidOrder;
            
        } catch (Exception e) {
            completeStep(step, false, e.getMessage());
            throw new RuntimeException("Failed to process payment", e);
        }
    }
    
    /**
     * Step 3: Analisar risco.
     */
    private Order executeStep3_AnalyzeRisk(OrderSagaCommand command, Order paidOrder, SagaExecutionEntity saga) {
        SagaStepEntity step = startStep(saga, "RISK_ANALYZED");
        
        try {
            // Análise de risco pode falhar, mas não é crítica
            Order analyzedOrder = analyzeRiskUseCase.execute(command.toAnalyzeRiskCommand(paidOrder.getId()));
            completeStep(step, true, null);
            saga.setStatus(SagaExecutionEntity.SagaStatus.RISK_ANALYZED);
            sagaRepository.save(saga);
            
            log.info("Step 3 completed: Risk analyzed - Risk Level: {}", analyzedOrder.getRiskLevel());
            return analyzedOrder;
            
        } catch (Exception e) {
            // Análise de risco falhou, mas não compensamos (não é crítica)
            log.warn("Risk analysis failed, but continuing: {}", e.getMessage());
            completeStep(step, false, e.getMessage());
            // Retorna pedido mesmo sem análise de risco
            return paidOrder;
        }
    }
    
    /**
     * Compensa a saga em caso de falha.
     * 
     * <p>Se o pedido foi criado mas pagamento falhou, cancela o pedido.</p>
     */
    private void compensate(SagaExecutionEntity saga, Order order, String reason) {
        log.warn("Compensating saga {} - Reason: {}", saga.getId(), reason);
        
        if (order != null && saga.getStatus() == SagaExecutionEntity.SagaStatus.ORDER_CREATED) {
            // Pedido foi criado mas pagamento falhou - cancelar pedido
            try {
                order.updateStatus(OrderStatus.CANCELED);
                orderRepository.save(order);
                log.info("Order {} cancelled due to payment failure", order.getId());
            } catch (Exception e) {
                log.error("Failed to compensate order {}: {}", order.getId(), e.getMessage());
            }
        }
        
        saga.setStatus(SagaExecutionEntity.SagaStatus.COMPENSATED);
        saga.setErrorMessage(reason);
        completeSaga(saga, order);
    }
    
    /**
     * Inicia uma nova execução de saga.
     */
    private SagaExecutionEntity startSaga() {
        SagaExecutionEntity saga = SagaExecutionEntity.builder()
            .id(UUID.randomUUID())
            .status(SagaExecutionEntity.SagaStatus.STARTED)
            .startedAt(LocalDateTime.now())
            .steps(new java.util.ArrayList<>())
            .build();
        return sagaRepository.save(saga);
    }
    
    /**
     * Marca saga como concluída.
     */
    private void completeSaga(SagaExecutionEntity saga, Order order) {
        saga.setStatus(SagaExecutionEntity.SagaStatus.COMPLETED);
        saga.setCompletedAt(LocalDateTime.now());
        
        if (saga.getStartedAt() != null && saga.getCompletedAt() != null) {
            long duration = java.time.Duration.between(saga.getStartedAt(), saga.getCompletedAt()).toMillis();
            saga.setDurationMs(duration);
        }
        
        sagaRepository.save(saga);
        log.info("Saga {} completed in {}ms", saga.getId(), saga.getDurationMs());
    }
    
    /**
     * Inicia um passo da saga.
     */
    private SagaStepEntity startStep(SagaExecutionEntity saga, String stepName) {
        SagaStepEntity step = SagaStepEntity.builder()
            .id(UUID.randomUUID())
            .sagaExecution(saga)
            .stepName(stepName)
            .status(SagaStepEntity.StepStatus.STARTED)
            .startedAt(LocalDateTime.now())
            .build();
        
        saga.getSteps().add(step);
        saga.setCurrentStep(stepName);
        sagaRepository.save(saga);
        
        return step;
    }
    
    /**
     * Completa um passo da saga.
     */
    private void completeStep(SagaStepEntity step, boolean success, String errorMessage) {
        step.setStatus(success ? SagaStepEntity.StepStatus.SUCCESS : SagaStepEntity.StepStatus.FAILED);
        step.setCompletedAt(LocalDateTime.now());
        
        if (step.getStartedAt() != null && step.getCompletedAt() != null) {
            long duration = java.time.Duration.between(step.getStartedAt(), step.getCompletedAt()).toMillis();
            step.setDurationMs(duration);
        }
        
        if (errorMessage != null) {
            step.setErrorMessage(errorMessage);
        }
        
        sagaRepository.save(step.getSagaExecution());
    }
}

