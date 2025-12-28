package com.marcelo.orchestrator.domain.event.saga;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SagaCompletedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final UUID aggregateId; // Order ID
    private final LocalDateTime occurredAt;
    private final String eventType = "SagaCompleted";
    
    private final UUID orderId;
    private final UUID sagaId;
    private final String orderStatus;
    private final String riskLevel;
    private final Long durationMs; // Duração total da saga em milissegundos
    
    public static SagaCompletedEvent from(Order order, UUID sagaId, Long durationMs) {
        return SagaCompletedEvent.builder()
            .eventId(UUID.randomUUID())
            .aggregateId(order.getId())
            .occurredAt(LocalDateTime.now())
            .orderId(order.getId())
            .sagaId(sagaId)
            .orderStatus(order.getStatus().name())
            .riskLevel(order.getRiskLevel() != null ? order.getRiskLevel().name() : "PENDING")
            .durationMs(durationMs)
            .build();
    }
    
    @Override
    public String getEventVersion() {
        return "1.0";
    }
}

