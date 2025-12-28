package com.marcelo.orchestrator.domain.port;

import com.marcelo.orchestrator.domain.event.DomainEvent;

public interface EventPublisherPort {
    
    <T extends DomainEvent> void publish(T event);

    <T extends DomainEvent> void publishBatch(java.util.List<T> events);
}

