package com.marcelo.orchestrator.domain.event.saga;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento publicado quando a saga falha.
 * 
 * <p>Implementa o padrão <strong>Domain Event</strong> e é publicado pelo
 * {@link com.marcelo.orchestrator.application.saga.OrderSagaOrchestrator}
 * quando algum step falha e a compensação é executada.</p>
 * 
 * <h3>Padrão: Domain Event</h3>
 * <p>Este evento indica que a saga falhou e foi compensada.
 * Consumidores devem reagir adequadamente (ex: notificar cliente,
 * liberar recursos reservados, etc.).</p>
 * 
 * <h3>Consumidores Típicos:</h3>
 * <ul>
 *   <li><strong>Notification Service:</strong> Envia email de falha ao cliente</li>
 *   <li><strong>Inventory Service:</strong> Libera estoque reservado</li>
 *   <li><strong>Analytics Service:</strong> Registra métrica de falha</li>
 *   <li><strong>Alerting Service:</strong> Envia alerta se taxa de falha alta</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class SagaFailedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final UUID aggregateId; // Order ID (pode ser null se falhou antes de criar pedido)
    private final LocalDateTime occurredAt;
    private final String eventType = "SagaFailed";
    
    private final UUID sagaId;
    private final UUID orderId; // null se falhou antes de criar pedido
    private final String failureReason;
    private final String failedStep; // "ORDER_CREATED", "PAYMENT_PROCESSED", etc.
    private final boolean compensated; // true se compensação foi executada
    
    public static SagaFailedEvent of(UUID sagaId, UUID orderId, String failureReason, 
                                     String failedStep, boolean compensated) {
        return SagaFailedEvent.builder()
            .eventId(UUID.randomUUID())
            .aggregateId(orderId) // Pode ser null
            .occurredAt(LocalDateTime.now())
            .sagaId(sagaId)
            .orderId(orderId)
            .failureReason(failureReason)
            .failedStep(failedStep)
            .compensated(compensated)
            .build();
    }
    
    @Override
    public String getEventVersion() {
        return "1.0";
    }
}

