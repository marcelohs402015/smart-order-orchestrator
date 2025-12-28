package com.marcelo.orchestrator.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent { 

    UUID getEventId();

    UUID getAggregateId();

    LocalDateTime getOccurredAt();
    
    String getEventType();
    
    default String getEventVersion() {
        return "1.0";
    }
}

