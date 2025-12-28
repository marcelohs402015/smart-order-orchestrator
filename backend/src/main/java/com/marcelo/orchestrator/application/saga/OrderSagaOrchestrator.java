package com.marcelo.orchestrator.application.saga;

import com.marcelo.orchestrator.application.usecase.AnalyzeRiskUseCase;
import com.marcelo.orchestrator.application.usecase.CreateOrderUseCase;
import com.marcelo.orchestrator.application.usecase.ProcessPaymentUseCase;
import com.marcelo.orchestrator.domain.event.saga.OrderCreatedEvent;
import com.marcelo.orchestrator.domain.event.saga.PaymentProcessedEvent;
import com.marcelo.orchestrator.domain.event.saga.SagaCompletedEvent;
import com.marcelo.orchestrator.domain.event.saga.SagaFailedEvent;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.application.exception.DomainException;
import com.marcelo.orchestrator.application.exception.OrderNotFoundException;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.domain.port.SagaExecutionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaOrchestrator {
    
    private final CreateOrderUseCase createOrderUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final AnalyzeRiskUseCase analyzeRiskUseCase;
    private final OrderRepositoryPort orderRepository;
    private final SagaExecutionRepositoryPort sagaRepository;

    private final EventPublisherPort eventPublisher;
    
    public OrderSagaResult execute(OrderSagaCommand command) {
        // Padrão: Idempotência - Verificar se saga já foi executada
        if (command.getIdempotencyKey() != null && !command.getIdempotencyKey().isBlank()) {
            Optional<SagaExecutionRepositoryPort.SagaExecution> existingSaga = sagaRepository
                .findByIdempotencyKey(command.getIdempotencyKey());
            
            if (existingSaga.isPresent()) {
                SagaExecutionRepositoryPort.SagaExecution saga = existingSaga.get();
                log.info("Found existing saga with idempotency key: {} - Status: {}", 
                    command.getIdempotencyKey(), saga.status());
                
                // Se já completou, retornar resultado anterior
                if (saga.status() == SagaExecutionRepositoryPort.SagaExecution.SagaStatus.COMPLETED && saga.orderId() != null) {
                    Order order = orderRepository.findById(saga.orderId())
                        .orElseThrow(() -> new OrderNotFoundException("Order not found for completed saga: " + saga.orderId()));
                    log.info("Returning existing completed saga result for idempotency key: {}", 
                        command.getIdempotencyKey());
                    return OrderSagaResult.success(order, saga.id());
                }
                
                // Se está em progresso ou falhou mas pode retry, retornar status atual
                if (saga.status() == SagaExecutionRepositoryPort.SagaExecution.SagaStatus.STARTED ||
                    saga.status() == SagaExecutionRepositoryPort.SagaExecution.SagaStatus.ORDER_CREATED ||
                    saga.status() == SagaExecutionRepositoryPort.SagaExecution.SagaStatus.PAYMENT_PROCESSED ||
                    saga.status() == SagaExecutionRepositoryPort.SagaExecution.SagaStatus.RISK_ANALYZED) {
                    log.info("Saga already in progress for idempotency key: {}", command.getIdempotencyKey());
                    return OrderSagaResult.inProgress(saga.id());
                }
                
                // Se falhou e foi compensada, pode tentar novamente (nova saga)
                // Mas por segurança, retornamos erro para evitar loops
                if (saga.status() == SagaExecutionRepositoryPort.SagaExecution.SagaStatus.COMPENSATED ||
                    saga.status() == SagaExecutionRepositoryPort.SagaExecution.SagaStatus.FAILED) {
                    log.warn("Previous saga failed for idempotency key: {}. Creating new saga.", 
                        command.getIdempotencyKey());
                    // Continua para criar nova saga (pode ser que queira retry)
                }
            }
        }
        
        // Iniciar nova saga (ou retry após falha)
        // IMPORTANTE: Tratar race condition - se duas requisições com mesma idempotencyKey
        // chegarem simultaneamente, apenas uma deve criar a saga
        SagaExecutionRepositoryPort.SagaExecution saga = startSagaSafely(command.getIdempotencyKey());
        log.info("Starting new order saga: {} (idempotency key: {})", 
            saga.id(), command.getIdempotencyKey());
        
        try {
            // Step 1: Criar pedido
            Order order = executeStep1_CreateOrder(command, saga);
            saga = sagaRepository.findById(saga.id()).orElse(saga); // Recarregar saga atualizada
            
            // Step 2: Processar pagamento
            Order paidOrder = executeStep2_ProcessPayment(command, order, saga);
            saga = sagaRepository.findById(saga.id()).orElse(saga); // Recarregar saga atualizada
            
            // Step 3: Analisar risco (apenas se pagamento sucedeu)
            if (paidOrder.isPaid()) {
                Order analyzedOrder = executeStep3_AnalyzeRisk(command, paidOrder, saga);
                saga = sagaRepository.findById(saga.id()).orElse(saga); // Recarregar saga atualizada
                saga = completeSaga(saga, analyzedOrder);
                
                // Padrão: Event-Driven Architecture - Publica evento de sucesso
                publishSagaCompletedEvent(saga, analyzedOrder);
                
                return OrderSagaResult.success(analyzedOrder, saga.id());
            } else if (paidOrder.getStatus() == OrderStatus.PAYMENT_PENDING) {
                // Pagamento ainda pendente: não compensamos, retornamos estado intermediário
                log.info("Saga finished with payment pending for order {}. SagaId={}", order.getId(), saga.id());
                return OrderSagaResult.inProgress(saga.id(), paidOrder);
            } else {
                // Compensação: cancelar pedido se pagamento falhou
                saga = compensate(saga, paidOrder, "Payment failed");
                return OrderSagaResult.failed(paidOrder, saga.id(), "Payment failed");
            }
            
        } catch (DomainException e) {
            log.error("Saga execution failed with domain error: {}", e.getMessage(), e);
            saga = compensate(saga, null, e.getMessage());
            return OrderSagaResult.failed(null, saga.id(), e.getMessage());
        } catch (Exception e) {
            log.error("Saga execution failed with unexpected error: {}", e.getMessage(), e);
            saga = compensate(saga, null, "Unexpected error: " + e.getMessage());
            return OrderSagaResult.failed(null, saga.id(), "Unexpected error: " + e.getMessage());
        }
    }
    
    private Order executeStep1_CreateOrder(OrderSagaCommand command, SagaExecutionRepositoryPort.SagaExecution saga) {
        SagaExecutionRepositoryPort.SagaExecution updatedSaga = startStep(saga, "ORDER_CREATED");
        
        try {
            Order order = createOrderUseCase.execute(command.toCreateOrderCommand());
            updatedSaga = completeStep(updatedSaga, "ORDER_CREATED", true, null);
            updatedSaga = updateSagaOrderId(updatedSaga, order.getId());
            updatedSaga = updateSagaStatus(updatedSaga, SagaExecutionRepositoryPort.SagaExecution.SagaStatus.ORDER_CREATED);
            
            log.info("Step 1 completed: Order created - {}", order.getId());
            
            // Padrão: Event-Driven Architecture - Publica evento de criação
            publishOrderCreatedEvent(order, updatedSaga.id());
            
            return order;
            
        } catch (DomainException e) {
            updatedSaga = completeStep(updatedSaga, "ORDER_CREATED", false, e.getMessage());
            throw new DomainException("Failed to create order: " + e.getMessage(), e);
        } catch (Exception e) {
            updatedSaga = completeStep(updatedSaga, "ORDER_CREATED", false, e.getMessage());
            throw new DomainException("Failed to create order: " + e.getMessage(), e);
        }
    }

    private Order executeStep2_ProcessPayment(OrderSagaCommand command, Order order, SagaExecutionRepositoryPort.SagaExecution saga) {
        SagaExecutionRepositoryPort.SagaExecution updatedSaga = startStep(saga, "PAYMENT_PROCESSED");
        
        try {
            Order paidOrder = processPaymentUseCase.execute(command.toProcessPaymentCommand(order.getId()));
            
            boolean paymentSucceeded = paidOrder.isPaid();
            boolean paymentPending = paidOrder.getStatus() == OrderStatus.PAYMENT_PENDING;
            
            String errorMessage = paymentSucceeded || paymentPending ? null : "Payment failed";
            updatedSaga = completeStep(updatedSaga, "PAYMENT_PROCESSED", paymentSucceeded || paymentPending, errorMessage);
            updatedSaga = updateSagaStatus(updatedSaga, SagaExecutionRepositoryPort.SagaExecution.SagaStatus.PAYMENT_PROCESSED);
            
            log.info("Step 2 completed: Payment processed - Status: {}", paidOrder.getStatus());
            
            // Padrão: Event-Driven Architecture - Publica evento de pagamento (sucesso ou falha)
            publishPaymentProcessedEvent(paidOrder, updatedSaga.id());
            
            return paidOrder;
            
        } catch (DomainException e) {
            updatedSaga = completeStep(updatedSaga, "PAYMENT_PROCESSED", false, e.getMessage());
            throw new DomainException("Failed to process payment: " + e.getMessage(), e);
        } catch (Exception e) {
            updatedSaga = completeStep(updatedSaga, "PAYMENT_PROCESSED", false, e.getMessage());
            throw new DomainException("Failed to process payment: " + e.getMessage(), e);
        }
    }
    
    private Order executeStep3_AnalyzeRisk(OrderSagaCommand command, Order paidOrder, SagaExecutionRepositoryPort.SagaExecution saga) {
        SagaExecutionRepositoryPort.SagaExecution updatedSaga = startStep(saga, "RISK_ANALYZED");
        
        try {
            // Análise de risco pode falhar, mas não é crítica
            Order analyzedOrder = analyzeRiskUseCase.execute(command.toAnalyzeRiskCommand(paidOrder.getId()));
            updatedSaga = completeStep(updatedSaga, "RISK_ANALYZED", true, null);
            updatedSaga = updateSagaStatus(updatedSaga, SagaExecutionRepositoryPort.SagaExecution.SagaStatus.RISK_ANALYZED);
            
            log.info("Step 3 completed: Risk analyzed - Risk Level: {}", analyzedOrder.getRiskLevel());
            return analyzedOrder;
            
        } catch (Exception e) {
            // Análise de risco falhou, mas não compensamos (não é crítica)
            log.warn("Risk analysis failed, but continuing: {}", e.getMessage());
            updatedSaga = completeStep(updatedSaga, "RISK_ANALYZED", false, e.getMessage());
            // Retorna pedido mesmo sem análise de risco
            return paidOrder;
        }
    }
    
    @Transactional
    private SagaExecutionRepositoryPort.SagaExecution compensate(
            SagaExecutionRepositoryPort.SagaExecution saga, Order order, String reason) {
        log.warn("Compensating saga {} - Reason: {}", saga.id(), reason);
        
        String failedStep = saga.currentStep() != null ? saga.currentStep() : "UNKNOWN";
        boolean compensated = compensateOrder(order);
        
        SagaExecutionRepositoryPort.SagaExecution updatedSaga = new SagaExecutionRepositoryPort.SagaExecution(
            saga.id(),
            saga.idempotencyKey(),
            order != null ? order.getId() : saga.orderId(),
            SagaExecutionRepositoryPort.SagaExecution.SagaStatus.COMPENSATED,
            saga.currentStep(),
            reason,
            saga.startedAt(),
            LocalDateTime.now(),
            saga.durationMs(),
            saga.steps()
        );
        
        updatedSaga = sagaRepository.save(updatedSaga);
        
        // Padrão: Event-Driven Architecture - Publica evento de falha
        publishSagaFailedEvent(updatedSaga, order, reason, failedStep, compensated);
        
        return updatedSaga;
    }
    
    private boolean compensateOrder(Order order) {
        if (order == null || order.isPaid()) {
            return false;
        }
        
        try {
            // Se já é PAYMENT_FAILED, manter o status (já foi salvo pelo ProcessPaymentUseCase)
            if (order.isPaymentFailed()) {
                // Status já está correto (PAYMENT_FAILED), apenas garantir que está salvo
                orderRepository.save(order);
                log.info("Order {} has PAYMENT_FAILED status - maintaining status", order.getId());
                return true;
            } else if (order.isPending()) {
                // Outros tipos de falha (não relacionadas a pagamento) - cancelar
                order.updateStatus(OrderStatus.CANCELED);
                orderRepository.save(order);
                log.info("Order {} cancelled due to failure", order.getId());
                return true;
            }
            // Se já é CANCELED, não fazer nada
            return false;
        } catch (Exception e) {
            log.error("Failed to compensate order {}: {}", order.getId(), e.getMessage());
            return false;
        }
    }
    
    private void publishOrderCreatedEvent(Order order, UUID sagaId) {
        try {
            OrderCreatedEvent event = OrderCreatedEvent.from(order, sagaId);
            eventPublisher.publish(event);
            log.debug("OrderCreatedEvent published for order {}", order.getId());
        } catch (Exception e) {
            // Padrão: Fail-Safe - loga erro mas não lança exceção
            log.error("Failed to publish OrderCreatedEvent: {}", e.getMessage(), e);
        }
    }
    

    private void publishPaymentProcessedEvent(Order order, UUID sagaId) {
        try {
            PaymentProcessedEvent event = PaymentProcessedEvent.from(order, sagaId);
            eventPublisher.publish(event);
            log.debug("PaymentProcessedEvent published for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentProcessedEvent: {}", e.getMessage(), e);
        }
    }
    

    private void publishSagaCompletedEvent(SagaExecutionRepositoryPort.SagaExecution saga, Order order) {
        try {
            SagaCompletedEvent event = SagaCompletedEvent.from(order, saga.id(), saga.durationMs());
            eventPublisher.publish(event);
            log.debug("SagaCompletedEvent published for saga {}", saga.id());
        } catch (Exception e) {
            log.error("Failed to publish SagaCompletedEvent: {}", e.getMessage(), e);
        }
    }
    

    private void publishSagaFailedEvent(SagaExecutionRepositoryPort.SagaExecution saga, Order order, String reason, 
                                        String failedStep, boolean compensated) {
        try {
            UUID orderId = order != null ? order.getId() : null;
            SagaFailedEvent event = SagaFailedEvent.of(saga.id(), orderId, reason, failedStep, compensated);
            eventPublisher.publish(event);
            log.debug("SagaFailedEvent published for saga {}", saga.id());
        } catch (Exception e) {
            log.error("Failed to publish SagaFailedEvent: {}", e.getMessage(), e);
        }
    }
    

    @Transactional
    private SagaExecutionRepositoryPort.SagaExecution startSagaSafely(String idempotencyKey) {
        try {
            SagaExecutionRepositoryPort.SagaExecution saga = new SagaExecutionRepositoryPort.SagaExecution(
                UUID.randomUUID(),
                idempotencyKey,
                null,
                SagaExecutionRepositoryPort.SagaExecution.SagaStatus.STARTED,
                null,
                null,
                LocalDateTime.now(),
                null,
                null,
                new java.util.ArrayList<>()
            );
            return sagaRepository.save(saga);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Race condition: outra thread já criou saga com mesma idempotencyKey
            // Verificar novamente e retornar a saga existente
            log.warn("Race condition detected for idempotency key: {}. Another thread already created the saga.", 
                idempotencyKey);
            
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                Optional<SagaExecutionRepositoryPort.SagaExecution> existingSaga = sagaRepository
                    .findByIdempotencyKey(idempotencyKey);
                
                if (existingSaga.isPresent()) {
                    log.info("Returning existing saga created by another thread: {}", existingSaga.get().id());
                    return existingSaga.get();
                }
            }
            
            // Se não encontrou, relançar exceção (erro inesperado)
            throw new DomainException("Failed to create saga and could not find existing saga", e);
        }
    }
    
    @Deprecated
    @Transactional
    private SagaExecutionRepositoryPort.SagaExecution startSaga(String idempotencyKey) {
        return startSagaSafely(idempotencyKey);
    }
    
     @Transactional
    private SagaExecutionRepositoryPort.SagaExecution completeSaga(
            SagaExecutionRepositoryPort.SagaExecution saga, Order order) {
        LocalDateTime completedAt = LocalDateTime.now();
        Long durationMs = null;
        
        if (saga.startedAt() != null && completedAt != null) {
            durationMs = java.time.Duration.between(saga.startedAt(), completedAt).toMillis();
        }
        
        SagaExecutionRepositoryPort.SagaExecution completedSaga = new SagaExecutionRepositoryPort.SagaExecution(
            saga.id(),
            saga.idempotencyKey(),
            order != null ? order.getId() : saga.orderId(),
            SagaExecutionRepositoryPort.SagaExecution.SagaStatus.COMPLETED,
            saga.currentStep(),
            saga.errorMessage(),
            saga.startedAt(),
            completedAt,
            durationMs,
            saga.steps()
        );
        
        SagaExecutionRepositoryPort.SagaExecution saved = sagaRepository.save(completedSaga);
        log.info("Saga {} completed in {}ms", saved.id(), saved.durationMs());
        return saved;
    }
    
    @Transactional
    private SagaExecutionRepositoryPort.SagaExecution startStep(
            SagaExecutionRepositoryPort.SagaExecution saga, String stepName) {
        UUID stepId = UUID.randomUUID();
        LocalDateTime stepStartedAt = LocalDateTime.now();
        
        SagaExecutionRepositoryPort.SagaStep step = new SagaExecutionRepositoryPort.SagaStep(
            stepId,
            saga.id(),
            stepName,
            SagaExecutionRepositoryPort.SagaStep.StepStatus.STARTED,
            null,
            stepStartedAt,
            null,
            null
        );
        
        List<SagaExecutionRepositoryPort.SagaStep> updatedSteps = new java.util.ArrayList<>(saga.steps());
        updatedSteps.add(step);
        
        SagaExecutionRepositoryPort.SagaExecution updatedSaga = new SagaExecutionRepositoryPort.SagaExecution(
            saga.id(),
            saga.idempotencyKey(),
            saga.orderId(),
            saga.status(),
            stepName,
            saga.errorMessage(),
            saga.startedAt(),
            saga.completedAt(),
            saga.durationMs(),
            updatedSteps
        );
        
        return sagaRepository.save(updatedSaga);
    }
    
    @Transactional
    private SagaExecutionRepositoryPort.SagaExecution completeStep(
            SagaExecutionRepositoryPort.SagaExecution saga, String stepName, 
            boolean success, String errorMessage) {
        LocalDateTime stepCompletedAt = LocalDateTime.now();
        
        // Encontrar o step atual e atualizá-lo
        List<SagaExecutionRepositoryPort.SagaStep> updatedSteps = saga.steps().stream()
            .map(step -> {
                if (step.stepName().equals(stepName) && step.status() == SagaExecutionRepositoryPort.SagaStep.StepStatus.STARTED) {
                    Long durationMs = null;
                    if (step.startedAt() != null && stepCompletedAt != null) {
                        durationMs = java.time.Duration.between(step.startedAt(), stepCompletedAt).toMillis();
                    }
                    
                    return new SagaExecutionRepositoryPort.SagaStep(
                        step.id(),
                        step.sagaExecutionId(),
                        step.stepName(),
                        success ? SagaExecutionRepositoryPort.SagaStep.StepStatus.SUCCESS 
                                : SagaExecutionRepositoryPort.SagaStep.StepStatus.FAILED,
                        errorMessage,
                        step.startedAt(),
                        stepCompletedAt,
                        durationMs
                    );
                }
                return step;
            })
            .toList();
        
        SagaExecutionRepositoryPort.SagaExecution updatedSaga = new SagaExecutionRepositoryPort.SagaExecution(
            saga.id(),
            saga.idempotencyKey(),
            saga.orderId(),
            saga.status(),
            saga.currentStep(),
            saga.errorMessage(),
            saga.startedAt(),
            saga.completedAt(),
            saga.durationMs(),
            updatedSteps
        );
        
        return sagaRepository.save(updatedSaga);
    }
    
    @Transactional
    private SagaExecutionRepositoryPort.SagaExecution updateSagaOrderId(
            SagaExecutionRepositoryPort.SagaExecution saga, UUID orderId) {
        SagaExecutionRepositoryPort.SagaExecution updatedSaga = new SagaExecutionRepositoryPort.SagaExecution(
            saga.id(),
            saga.idempotencyKey(),
            orderId,
            saga.status(),
            saga.currentStep(),
            saga.errorMessage(),
            saga.startedAt(),
            saga.completedAt(),
            saga.durationMs(),
            saga.steps()
        );
        return sagaRepository.save(updatedSaga);
    }
    
    @Transactional
    private SagaExecutionRepositoryPort.SagaExecution updateSagaStatus(
            SagaExecutionRepositoryPort.SagaExecution saga, 
            SagaExecutionRepositoryPort.SagaExecution.SagaStatus status) {
        SagaExecutionRepositoryPort.SagaExecution updatedSaga = new SagaExecutionRepositoryPort.SagaExecution(
            saga.id(),
            saga.idempotencyKey(),
            saga.orderId(),
            status,
            saga.currentStep(),
            saga.errorMessage(),
            saga.startedAt(),
            saga.completedAt(),
            saga.durationMs(),
            saga.steps()
        );
        return sagaRepository.save(updatedSaga);
    }
}

