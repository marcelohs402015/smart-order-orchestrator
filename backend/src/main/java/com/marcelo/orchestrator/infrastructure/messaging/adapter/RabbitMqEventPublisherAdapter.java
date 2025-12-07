package com.marcelo.orchestrator.infrastructure.messaging.adapter;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter para publicação de eventos no RabbitMQ.
 * 
 * <p>Implementa o padrão <strong>Adapter Pattern</strong> da Hexagonal Architecture,
 * adaptando a interface EventPublisherPort para usar RabbitMQ como message broker.</p>
 * 
 * <h3>Padrão: Adapter Pattern</h3>
 * <ul>
 *   <li><strong>Adapter:</strong> Esta classe - adapta EventPublisherPort para RabbitMQ</li>
 *   <li><strong>Target:</strong> EventPublisherPort (interface do domínio)</li>
 *   <li><strong>Adaptee:</strong> RabbitTemplate (API do Spring AMQP)</li>
 * </ul>
 * 
 * <h3>Por que RabbitMQ?</h3>
 * <ul>
 *   <li><strong>AMQP Standard:</strong> Protocolo padrão da indústria</li>
 *   <li><strong>Fácil de Usar:</strong> Interface simples e intuitiva</li>
 *   <li><strong>Flexível:</strong> Suporta diferentes padrões (queues, exchanges, routing)</li>
 *   <li><strong>Confiável:</strong> Garante entrega de mensagens</li>
 *   <li><strong>Management UI:</strong> Interface web para monitoramento</li>
 * </ul>
 * 
 * <h3>Configuração:</h3>
 * <pre>{@code
 * # application.yml
 * app:
 *   message:
 *     broker:
 *       type: RABBITMQ
 * 
 * spring:
 *   rabbitmq:
 *     host: localhost
 *     port: 5672
 *     username: guest
 *     password: guest
 * }</pre>
 * 
 * <h3>Roteamento:</h3>
 * <p>Usa exchanges e routing keys para rotear eventos:</p>
 * <ul>
 *   <li>Exchange: "domain-events" (topic exchange)</li>
 *   <li>Routing Key: "order.created", "payment.processed", etc.</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.message.broker.type", havingValue = "RABBITMQ")
public class RabbitMqEventPublisherAdapter implements EventPublisherPort {
    
    // TODO: Injetar RabbitTemplate quando dependência for adicionada
    // private final RabbitTemplate rabbitTemplate;
    
    /**
     * Construtor (placeholder para quando RabbitTemplate for adicionado).
     */
    public RabbitMqEventPublisherAdapter() {
        log.warn("RabbitMqEventPublisherAdapter created but not fully implemented. " +
                "Add Spring AMQP dependency to enable.");
    }
    
    /**
     * Publica evento no RabbitMQ.
     */
    @Override
    public <T extends DomainEvent> void publish(T event) {
        try {
            String exchange = "domain-events";
            String routingKey = getRoutingKeyForEvent(event);
            
            log.debug("Publishing event to RabbitMQ exchange '{}' with routing key '{}': {} (aggregateId: {})", 
                exchange, routingKey, event.getEventType(), event.getAggregateId());
            
            // TODO: Implementar quando RabbitTemplate estiver disponível
            // rabbitTemplate.convertAndSend(exchange, routingKey, event);
            
            log.warn("RabbitMQ publishing not yet implemented. Event {} would be published to exchange '{}' with routing key '{}'", 
                event.getEventType(), exchange, routingKey);
                
        } catch (Exception e) {
            log.error("Failed to publish event {} to RabbitMQ: {}", 
                event.getEventType(), e.getMessage(), e);
        }
    }
    
    /**
     * Publica múltiplos eventos em batch.
     */
    @Override
    public <T extends DomainEvent> void publishBatch(List<T> events) {
        try {
            log.debug("Publishing {} events to RabbitMQ in batch", events.size());
            
            events.forEach(this::publish);
            
        } catch (Exception e) {
            log.error("Failed to publish batch to RabbitMQ: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Mapeia tipo de evento para routing key do RabbitMQ.
     */
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

