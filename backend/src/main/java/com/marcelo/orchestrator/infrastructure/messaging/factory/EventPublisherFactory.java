package com.marcelo.orchestrator.infrastructure.messaging.factory;

import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.infrastructure.messaging.adapter.InMemoryEventPublisherAdapter;
import com.marcelo.orchestrator.infrastructure.messaging.adapter.KafkaEventPublisherAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class EventPublisherFactory {
    
    
    
    
    @Value("${app.message.broker.type:IN_MEMORY}")
    private String brokerType;
    
    private final InMemoryEventPublisherAdapter inMemoryAdapter;
    private final KafkaEventPublisherAdapter kafkaAdapter;
    
    
    public EventPublisherFactory(
            InMemoryEventPublisherAdapter inMemoryAdapter,
            @org.springframework.beans.factory.annotation.Autowired(required = false) KafkaEventPublisherAdapter kafkaAdapter) {
        this.inMemoryAdapter = inMemoryAdapter;
        this.kafkaAdapter = kafkaAdapter;
    }
    
    
    public EventPublisherPort create() {
        MessageBrokerType type = MessageBrokerType.fromString(brokerType);
        
        log.info("Creating EventPublisherPort for broker type: {}", type);
        
        return switch (type) {
            case IN_MEMORY -> {
                log.debug("Using InMemoryEventPublisherAdapter (for tests/dev)");
                yield inMemoryAdapter;
            }
            case KAFKA -> {
                if (kafkaAdapter == null) {
                    log.warn("KafkaEventPublisherAdapter not available, falling back to IN_MEMORY");
                    yield inMemoryAdapter;
                }
                log.debug("Using KafkaEventPublisherAdapter");
                yield kafkaAdapter;
            }
            case RABBITMQ, SQS -> {
                log.warn("Broker type '{}' not yet implemented, falling back to IN_MEMORY", type);
                yield inMemoryAdapter;
            }
        };
    }
    
    
    public enum MessageBrokerType {
        IN_MEMORY("IN_MEMORY"),
        KAFKA("KAFKA"),
        RABBITMQ("RABBITMQ"),
        SQS("SQS");
        
        private final String value;
        
        MessageBrokerType(String value) {
            this.value = value;
        }
        
        public static MessageBrokerType fromString(String value) {
            for (MessageBrokerType type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unsupported message broker type: " + value);
        }
    }
}

