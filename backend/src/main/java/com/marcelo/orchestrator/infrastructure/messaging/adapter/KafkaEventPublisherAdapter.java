package com.marcelo.orchestrator.infrastructure.messaging.adapter;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.message.broker.type", havingValue = "KAFKA")
@RequiredArgsConstructor
public class KafkaEventPublisherAdapter implements EventPublisherPort {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${app.message.broker.kafka.topics.order-created}")
    private String orderCreatedTopic;
    
    @Value("${app.message.broker.kafka.topics.payment-processed}")
    private String paymentProcessedTopic;
    
    @Value("${app.message.broker.kafka.topics.saga-completed}")
    private String sagaCompletedTopic;
    
    @Value("${app.message.broker.kafka.topics.saga-failed}")
    private String sagaFailedTopic;

    @Override
    public <T extends DomainEvent> void publish(T event) {
        try {
            String topic = getTopicForEvent(event.getEventType());
            String key = event.getAggregateId() != null 
                ? event.getAggregateId().toString() 
                : event.getEventId().toString();
            
            log.debug("Publishing event to Kafka topic '{}': {} (aggregateId: {}, eventId: {})", 
                topic, event.getEventType(), event.getAggregateId(), event.getEventId());
            
            
            var message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader("eventId", event.getEventId().toString())
                .setHeader("aggregateId", event.getAggregateId() != null 
                    ? event.getAggregateId().toString() 
                    : "")
                .setHeader("eventType", event.getEventType())
                .setHeader("eventVersion", event.getEventVersion())
                .setHeader("occurredAt", event.getOccurredAt().toString())
                .build();
            
            
            kafkaTemplate.send(message);
            
            log.info("Event published successfully to Kafka: {} [{}] → topic: {}", 
                event.getEventType(), event.getEventId(), topic);
                
        } catch (Exception e) {
            
            log.error("Failed to publish event {} to Kafka: {}", 
                event.getEventType(), e.getMessage(), e);
        }
    }
    
    @Override
    public <T extends DomainEvent> void publishBatch(List<T> events) {
        try {
            log.debug("Publishing {} events to Kafka in batch", events.size());
            
            for (T event : events) {
                publish(event); 
            }
            
            log.info("Batch of {} events published successfully to Kafka", events.size());
            
        } catch (Exception e) {
            log.error("Failed to publish batch of events to Kafka: {}", e.getMessage(), e);
        }
    }
    
    private String getTopicForEvent(String eventType) {
        return switch (eventType) {
            case "OrderCreated" -> orderCreatedTopic;
            case "PaymentProcessed" -> paymentProcessedTopic;
            case "SagaCompleted" -> sagaCompletedTopic;
            case "SagaFailed" -> sagaFailedTopic;
            default -> {
                log.warn("Unknown event type '{}', using default topic 'order-created'", eventType);
                yield orderCreatedTopic;
            }
        };
    }
}
