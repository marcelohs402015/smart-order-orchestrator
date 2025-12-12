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
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.infrastructure.persistence.entity.SagaExecutionEntity;
import com.marcelo.orchestrator.infrastructure.persistence.entity.SagaStepEntity;
import com.marcelo.orchestrator.infrastructure.persistence.repository.JpaSagaExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
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
 * <h3>Padrão: Event-Driven Architecture</h3>
 * <p>Este orchestrator publica eventos em cada step da saga, permitindo que outros serviços
 * reajam de forma assíncrona e desacoplada:</p>
 * <ul>
 *   <li><strong>OrderCreatedEvent:</strong> Publicado após Step 1 (Create Order)</li>
 *   <li><strong>PaymentProcessedEvent:</strong> Publicado após Step 2 (Process Payment)</li>
 *   <li><strong>SagaCompletedEvent:</strong> Publicado quando saga completa com sucesso</li>
 *   <li><strong>SagaFailedEvent:</strong> Publicado quando saga falha e é compensada</li>
 * </ul>
 * 
 * <h3>Padrão: Factory Pattern para Message Brokers</h3>
 * <p>Os eventos são publicados via {@link EventPublisherPort}, que é injetado usando
 * {@link com.marcelo.orchestrator.infrastructure.messaging.factory.EventPublisherFactory}.
 * Isso permite trocar message broker (Kafka, Pub/Sub, RabbitMQ) via configuração sem alterar código.</p>
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
     * Port para publicação de eventos (injetado via Factory Pattern).
     * 
     * <p>Padrão: Dependency Injection - Spring injeta a implementação correta
     * baseada na configuração (Kafka, Pub/Sub, RabbitMQ, ou In-Memory).</p>
     * 
     * <p>Padrão: Port (Hexagonal Architecture) - Interface do domínio que permite
     * publicar eventos sem conhecer detalhes do message broker.</p>
     */
    private final EventPublisherPort eventPublisher;
    
    /**
     * Executa a saga completa de criação de pedido.
     * 
     * <p>Orquestra os 3 passos sequencialmente, rastreando cada um
     * e executando compensação se necessário.</p>
     * 
     * <h3>Padrão: Idempotência</h3>
     * <p>Se uma saga com a mesma {@code idempotencyKey} já foi executada,
     * retorna o resultado anterior ao invés de criar novo pedido. Isso previne
     * duplicação em caso de retry, timeout ou usuário clicando várias vezes.</p>
     * 
     * <h3>Gerenciamento de Transações (Saga Pattern):</h3>
     * <p>Este método NÃO usa @Transactional porque segue os princípios do Saga Pattern:
     * - Cada passo (use case) é uma transação independente que faz commit imediato
     * - Se um passo falhar, os anteriores já foram commitados (não há rollback de transação)
     * - Compensação é executada manualmente (não via rollback de transação)
     * - Estado da saga é persistido em transações separadas para rastreamento
     * 
     * <p>Isso evita o problema de "rollback-only" que ocorre com transações aninhadas
     * e garante que cada passo seja atomicamente commitado, permitindo compensação
     * manual se necessário.</p>
     * 
     * @param command Command com todos os dados necessários (incluindo idempotencyKey)
     * @return Resultado da saga (sucesso, falha ou em progresso)
     */
    public OrderSagaResult execute(OrderSagaCommand command) {
        // Padrão: Idempotência - Verificar se saga já foi executada
        if (command.getIdempotencyKey() != null && !command.getIdempotencyKey().isBlank()) {
            Optional<SagaExecutionEntity> existingSaga = sagaRepository
                .findByIdempotencyKey(command.getIdempotencyKey());
            
            if (existingSaga.isPresent()) {
                SagaExecutionEntity saga = existingSaga.get();
                log.info("Found existing saga with idempotency key: {} - Status: {}", 
                    command.getIdempotencyKey(), saga.getStatus());
                
                // Se já completou, retornar resultado anterior
                if (saga.getStatus() == SagaExecutionEntity.SagaStatus.COMPLETED && saga.getOrderId() != null) {
                    Order order = orderRepository.findById(saga.getOrderId())
                        .orElseThrow(() -> new RuntimeException("Order not found for completed saga"));
                    log.info("Returning existing completed saga result for idempotency key: {}", 
                        command.getIdempotencyKey());
                    return OrderSagaResult.success(order, saga.getId());
                }
                
                // Se está em progresso ou falhou mas pode retry, retornar status atual
                if (saga.getStatus() == SagaExecutionEntity.SagaStatus.STARTED ||
                    saga.getStatus() == SagaExecutionEntity.SagaStatus.ORDER_CREATED ||
                    saga.getStatus() == SagaExecutionEntity.SagaStatus.PAYMENT_PROCESSED ||
                    saga.getStatus() == SagaExecutionEntity.SagaStatus.RISK_ANALYZED) {
                    log.info("Saga already in progress for idempotency key: {}", command.getIdempotencyKey());
                    return OrderSagaResult.inProgress(saga.getId());
                }
                
                // Se falhou e foi compensada, pode tentar novamente (nova saga)
                // Mas por segurança, retornamos erro para evitar loops
                if (saga.getStatus() == SagaExecutionEntity.SagaStatus.COMPENSATED ||
                    saga.getStatus() == SagaExecutionEntity.SagaStatus.FAILED) {
                    log.warn("Previous saga failed for idempotency key: {}. Creating new saga.", 
                        command.getIdempotencyKey());
                    // Continua para criar nova saga (pode ser que queira retry)
                }
            }
        }
        
        // Iniciar nova saga (ou retry após falha)
        SagaExecutionEntity saga = startSaga(command.getIdempotencyKey());
        log.info("Starting new order saga: {} (idempotency key: {})", 
            saga.getId(), command.getIdempotencyKey());
        
        try {
            // Step 1: Criar pedido
            Order order = executeStep1_CreateOrder(command, saga);
            
            // Step 2: Processar pagamento
            Order paidOrder = executeStep2_ProcessPayment(command, order, saga);
            
            // Step 3: Analisar risco (apenas se pagamento sucedeu)
            if (paidOrder.isPaid()) {
                Order analyzedOrder = executeStep3_AnalyzeRisk(command, paidOrder, saga);
                completeSaga(saga, analyzedOrder);
                
                // Padrão: Event-Driven Architecture - Publica evento de sucesso
                publishSagaCompletedEvent(saga, analyzedOrder);
                
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
     * 
     * <p>Padrão: Event-Driven Architecture - Após criar pedido com sucesso,
     * publica OrderCreatedEvent para notificar outros serviços.</p>
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
            
            // Padrão: Event-Driven Architecture - Publica evento de criação
            publishOrderCreatedEvent(order, saga.getId());
            
            return order;
            
        } catch (Exception e) {
            completeStep(step, false, e.getMessage());
            throw new RuntimeException("Failed to create order", e);
        }
    }
    
    /**
     * Step 2: Processar pagamento.
     * 
     * <p>Padrão: Event-Driven Architecture - Após processar pagamento,
     * publica PaymentProcessedEvent (sucesso ou falha) para notificar outros serviços.</p>
     */
    private Order executeStep2_ProcessPayment(OrderSagaCommand command, Order order, SagaExecutionEntity saga) {
        SagaStepEntity step = startStep(saga, "PAYMENT_PROCESSED");
        
        try {
            Order paidOrder = processPaymentUseCase.execute(command.toProcessPaymentCommand(order.getId()));
            completeStep(step, paidOrder.isPaid(), paidOrder.isPaid() ? null : "Payment failed");
            saga.setStatus(SagaExecutionEntity.SagaStatus.PAYMENT_PROCESSED);
            sagaRepository.save(saga);
            
            log.info("Step 2 completed: Payment processed - Status: {}", paidOrder.getStatus());
            
            // Padrão: Event-Driven Architecture - Publica evento de pagamento (sucesso ou falha)
            publishPaymentProcessedEvent(paidOrder, saga.getId());
            
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
     * <p>Se o pedido foi criado mas pagamento falhou, mantém o status PAYMENT_FAILED
     * para que o frontend possa identificar corretamente a causa da falha.</p>
     * 
     * <p>Padrão: Event-Driven Architecture - Após compensação,
     * publica SagaFailedEvent para notificar outros serviços sobre a falha.</p>
     */
    @Transactional
    private void compensate(SagaExecutionEntity saga, Order order, String reason) {
        log.warn("Compensating saga {} - Reason: {}", saga.getId(), reason);
        
        String failedStep = saga.getCurrentStep() != null ? saga.getCurrentStep() : "UNKNOWN";
        boolean compensated = false;
        
        // Se pedido existe e não foi pago, garantir que status está correto
        // Se já é PAYMENT_FAILED, manter (não mudar para CANCELED)
        // Se é PENDING, pode mudar para CANCELED (outros tipos de falha)
        if (order != null && !order.isPaid()) {
            try {
                // Se já é PAYMENT_FAILED, manter o status (já foi salvo pelo ProcessPaymentUseCase)
                if (order.isPaymentFailed()) {
                    // Status já está correto (PAYMENT_FAILED), apenas garantir que está salvo
                    orderRepository.save(order);
                    compensated = true;
                    log.info("Order {} has PAYMENT_FAILED status - maintaining status", order.getId());
                } else if (order.isPending()) {
                    // Outros tipos de falha (não relacionadas a pagamento) - cancelar
                    order.updateStatus(OrderStatus.CANCELED);
                    orderRepository.save(order);
                    compensated = true;
                    log.info("Order {} cancelled due to failure", order.getId());
                }
                // Se já é CANCELED, não fazer nada
            } catch (Exception e) {
                log.error("Failed to compensate order {}: {}", order.getId(), e.getMessage());
            }
        }
        
        saga.setStatus(SagaExecutionEntity.SagaStatus.COMPENSATED);
        saga.setErrorMessage(reason);
        completeSaga(saga, order);
        
        // Padrão: Event-Driven Architecture - Publica evento de falha
        publishSagaFailedEvent(saga, order, reason, failedStep, compensated);
    }
    
    /**
     * Publica OrderCreatedEvent após Step 1.
     * 
     * <p>Padrão: Fail-Safe - Se publicação falhar, não interrompe fluxo principal.</p>
     */
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
    
    /**
     * Publica PaymentProcessedEvent após Step 2.
     */
    private void publishPaymentProcessedEvent(Order order, UUID sagaId) {
        try {
            PaymentProcessedEvent event = PaymentProcessedEvent.from(order, sagaId);
            eventPublisher.publish(event);
            log.debug("PaymentProcessedEvent published for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentProcessedEvent: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publica SagaCompletedEvent quando saga completa com sucesso.
     */
    private void publishSagaCompletedEvent(SagaExecutionEntity saga, Order order) {
        try {
            SagaCompletedEvent event = SagaCompletedEvent.from(order, saga.getId(), saga.getDurationMs());
            eventPublisher.publish(event);
            log.debug("SagaCompletedEvent published for saga {}", saga.getId());
        } catch (Exception e) {
            log.error("Failed to publish SagaCompletedEvent: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publica SagaFailedEvent quando saga falha e é compensada.
     */
    private void publishSagaFailedEvent(SagaExecutionEntity saga, Order order, String reason, 
                                        String failedStep, boolean compensated) {
        try {
            UUID orderId = order != null ? order.getId() : null;
            SagaFailedEvent event = SagaFailedEvent.of(saga.getId(), orderId, reason, failedStep, compensated);
            eventPublisher.publish(event);
            log.debug("SagaFailedEvent published for saga {}", saga.getId());
        } catch (Exception e) {
            log.error("Failed to publish SagaFailedEvent: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Inicia uma nova execução de saga.
     * 
     * <p>Padrão: Idempotência - Salva a idempotencyKey na saga para
     * permitir verificação posterior de duplicatas.</p>
     * 
     * @param idempotencyKey Chave de idempotência (pode ser null)
     * @return Saga criada e persistida
     */
    @Transactional
    private SagaExecutionEntity startSaga(String idempotencyKey) {
        SagaExecutionEntity saga = SagaExecutionEntity.builder()
            .id(UUID.randomUUID())
            .idempotencyKey(idempotencyKey)
            .status(SagaExecutionEntity.SagaStatus.STARTED)
            .startedAt(LocalDateTime.now())
            .steps(new java.util.ArrayList<>())
            .build();
        return sagaRepository.save(saga);
    }
    
    /**
     * Marca saga como concluída.
     */
    @Transactional
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
    @Transactional
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
    @Transactional
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

