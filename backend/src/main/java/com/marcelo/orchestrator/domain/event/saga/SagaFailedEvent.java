package com.marcelo.orchestrator.domain.event.saga;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SagaFailedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final UUID aggregateId; 
    private final LocalDateTime occurredAt;
    private final String eventType = "SagaFailed";
    
    private final UUID sagaId;
    private final UUID orderId; 
    private final String failureReason;
    private final String failedStep; 
    private final boolean compensated; 
    
    public static SagaFailedEvent of(UUID sagaId, UUID orderId, String failureReason, 
                                     String failedStep, boolean compensated) {
        return SagaFailedEvent.builder()
            .eventId(UUID.randomUUID())
            .aggregateId(orderId) 
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

