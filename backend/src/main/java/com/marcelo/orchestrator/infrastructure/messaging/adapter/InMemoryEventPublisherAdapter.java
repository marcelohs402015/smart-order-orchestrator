package com.marcelo.orchestrator.infrastructure.messaging.adapter;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class InMemoryEventPublisherAdapter implements EventPublisherPort {

    private final List<DomainEvent> publishedEvents = new CopyOnWriteArrayList<>();
    
    @Override
    public <T extends DomainEvent> void publish(T event) {
        try {
            log.debug("Publishing event in-memory: {} (aggregateId: {})", 
                event.getEventType(), event.getAggregateId());
            
            publishedEvents.add(event);
            
            log.info("Event published successfully: {} [{}]", 
                event.getEventType(), event.getEventId());
                
        } catch (Exception e) {
            
            log.error("Failed to publish event {}: {}", event.getEventType(), e.getMessage(), e);
        }
    }
    
    @Override
    public <T extends DomainEvent> void publishBatch(List<T> events) {
        try {
            log.debug("Publishing {} events in batch", events.size());
            
            publishedEvents.addAll(events);
            
            log.info("Batch of {} events published successfully", events.size());
            
        } catch (Exception e) {
            log.error("Failed to publish batch of events: {}", e.getMessage(), e);
        }
    }
    
    public List<DomainEvent> getPublishedEvents() {
        return new ArrayList<>(publishedEvents);
    }
    
    public void clear() {
        publishedEvents.clear();
        log.debug("Cleared published events");
    }
    
    public long countByType(String eventType) {
        return publishedEvents.stream()
            .filter(event -> event.getEventType().equals(eventType))
            .count();
    }
}

