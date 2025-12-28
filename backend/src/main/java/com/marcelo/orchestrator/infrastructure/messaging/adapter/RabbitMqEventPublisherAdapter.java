package com.marcelo.orchestrator.infrastructure.messaging.adapter;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@ConditionalOnProperty(name = "app.message.broker.type", havingValue = "RABBITMQ")
public class RabbitMqEventPublisherAdapter implements EventPublisherPort {
    
    
    
    
    
    public RabbitMqEventPublisherAdapter() {
        log.warn("RabbitMqEventPublisherAdapter created but not fully implemented. " +
                "Add Spring AMQP dependency to enable.");
    }
    
    
    @Override
    public <T extends DomainEvent> void publish(T event) {
        try {
            String exchange = "domain-events";
            String routingKey = getRoutingKeyForEvent(event);
            
            log.debug("Publishing event to RabbitMQ exchange '{}' with routing key '{}': {} (aggregateId: {})", 
                exchange, routingKey, event.getEventType(), event.getAggregateId());
            
            
            
            
            log.warn("RabbitMQ publishing not yet implemented. Event {} would be published to exchange '{}' with routing key '{}'", 
                event.getEventType(), exchange, routingKey);
                
        } catch (Exception e) {
            log.error("Failed to publish event {} to RabbitMQ: {}", 
                event.getEventType(), e.getMessage(), e);
        }
    }
    
    
    @Override
    public <T extends DomainEvent> void publishBatch(List<T> events) {
        try {
            log.debug("Publishing {} events to RabbitMQ in batch", events.size());
            
            events.forEach(this::publish);
            
        } catch (Exception e) {
            log.error("Failed to publish batch to RabbitMQ: {}", e.getMessage(), e);
        }
    }
    
    
    private String getRoutingKeyForEvent(DomainEvent event) {
        return switch (event.getEventType()) {
            case "OrderCreated" -> "order.created";
            case "PaymentProcessed" -> "payment.processed";
            case "SagaCompleted" -> "saga.completed";
            case "SagaFailed" -> "saga.failed";
            default -> "domain.event";
        };
    }
}

