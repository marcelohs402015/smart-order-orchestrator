package com.marcelo.orchestrator.infrastructure.messaging.adapter;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// TODO: Descomentar quando dependência do Spring Kafka for adicionada
// import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

/**
 * Adapter para publicação de eventos no Apache Kafka.
 * 
 * <p>Implementa o padrão <strong>Adapter Pattern</strong> da Hexagonal Architecture,
 * adaptando a interface EventPublisherPort para usar Apache Kafka como message broker.</p>
 * 
 * <h3>Padrão: Adapter Pattern</h3>
 * <ul>
 *   <li><strong>Adapter:</strong> Esta classe - adapta EventPublisherPort para Kafka</li>
 *   <li><strong>Target:</strong> EventPublisherPort (interface do domínio)</li>
 *   <li><strong>Adaptee:</strong> KafkaTemplate (API do Spring Kafka)</li>
 * </ul>
 * 
 * <h3>Por que Kafka?</h3>
 * <ul>
 *   <li><strong>Alta Performance:</strong> Milhões de mensagens por segundo</li>
 *   <li><strong>Escalabilidade:</strong> Distribuído, suporta clusters grandes</li>
 *   <li><strong>Durabilidade:</strong> Mensagens são persistidas em disco</li>
 *   <li><strong>Replay:</strong> Permite reprocessar mensagens</li>
 *   <li><strong>Ordenação:</strong> Garante ordem dentro de uma partição</li>
 * </ul>
 * 
 * <h3>Configuração:</h3>
 * <pre>{@code
 * # application.yml
 * app:
 *   message:
 *     broker:
 *       type: KAFKA
 * 
 * spring:
 *   kafka:
 *     bootstrap-servers: localhost:9092
 *     producer:
 *       key-serializer: org.apache.kafka.common.serialization.StringSerializer
 *       value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
 * }</pre>
 * 
 * <h3>Roteamento de Tópicos:</h3>
 * <p>Cada tipo de evento é publicado em um tópico específico:</p>
 * <ul>
 *   <li>OrderCreatedEvent → tópico: "order-created"</li>
 *   <li>PaymentProcessedEvent → tópico: "payment-processed"</li>
 *   <li>SagaCompletedEvent → tópico: "saga-completed"</li>
 *   <li>SagaFailedEvent → tópico: "saga-failed"</li>
 * </ul>
 * 
 * <h3>Padrão: Topic-per-Event-Type</h3>
 * <p>Cada tipo de evento tem seu próprio tópico, permitindo:</p>
 * <ul>
 *   <li><strong>Isolamento:</strong> Consumidores só recebem eventos relevantes</li>
 *   <li><strong>Escalabilidade:</strong> Cada tópico pode ter diferentes configurações</li>
 *   <li><strong>Retenção:</strong> Diferentes políticas de retenção por tipo</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.message.broker.type", havingValue = "KAFKA")
public class KafkaEventPublisherAdapter implements EventPublisherPort {
    
    // TODO: Descomentar quando dependência do Spring Kafka for adicionada
    // private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    
    /**
     * Construtor (placeholder para quando KafkaTemplate for adicionado).
     * 
     * <p>Para ativar, adicionar dependência do Spring Kafka:</p>
     * <pre>{@code
     * <dependency>
     *     <groupId>org.springframework.kafka</groupId>
     *     <artifactId>spring-kafka</artifactId>
     * </dependency>
     * }</pre>
     */
    public KafkaEventPublisherAdapter() {
        // TODO: Injetar KafkaTemplate quando dependência for adicionada
        // this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Publica evento no Kafka.
     * 
     * <p>Padrão: Fail-Safe - se publicação falhar, loga erro mas não lança exceção,
     * garantindo que o fluxo principal da saga não seja interrompido.</p>
     * 
     * <p>Padrão: Async Publishing - KafkaTemplate.send() é assíncrono por padrão,
     * não bloqueia a thread principal.</p>
     */
    @Override
    public <T extends DomainEvent> void publish(T event) {
        try {
            String topic = getTopicForEvent(event);
            String key = event.getAggregateId().toString(); // Partition key = aggregate ID
            
            log.debug("Publishing event to Kafka topic '{}': {} (aggregateId: {})", 
                topic, event.getEventType(), event.getAggregateId());
            
            // TODO: Implementar quando KafkaTemplate estiver disponível
            // Padrão: Async Publishing - não bloqueia thread
            // kafkaTemplate.send(topic, key, event)
            //     .whenComplete((result, exception) -> {
            //         if (exception != null) {
            //             log.error("Failed to publish event {} to Kafka: {}", 
            //                 event.getEventType(), exception.getMessage(), exception);
            //         } else {
            //             log.info("Event published to Kafka successfully: {} [{}] on topic '{}'", 
            //                 event.getEventType(), event.getEventId(), topic);
            //         }
            //     });
            
            log.warn("Kafka publishing not yet implemented. Event {} would be published to topic '{}'", 
                event.getEventType(), topic);
                
        } catch (Exception e) {
            // Padrão: Fail-Safe
            log.error("Failed to publish event {} to Kafka: {}", 
                event.getEventType(), e.getMessage(), e);
        }
    }
    
    /**
     * Publica múltiplos eventos em batch.
     * 
     * <p>Padrão: Batch Processing - otimiza publicação usando sendBatch(),
     * reduzindo overhead de rede e melhorando throughput.</p>
     */
    @Override
    public <T extends DomainEvent> void publishBatch(List<T> events) {
        try {
            log.debug("Publishing {} events to Kafka in batch", events.size());
            
            // TODO: Implementar batch sending quando necessário
            // Por enquanto, envia sequencialmente
            events.forEach(this::publish);
            
            log.info("Batch of {} events published to Kafka", events.size());
            
        } catch (Exception e) {
            log.error("Failed to publish batch to Kafka: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Mapeia tipo de evento para tópico Kafka.
     * 
     * <p>Padrão: Strategy Pattern (implícito) - cada tipo de evento tem sua própria
     * estratégia de roteamento (tópico).</p>
     */
    private String getTopicForEvent(DomainEvent event) {
        return switch (event.getEventType()) {
            case "OrderCreated" -> "order-created";
            case "PaymentProcessed" -> "payment-processed";
            case "SagaCompleted" -> "saga-completed";
            case "SagaFailed" -> "saga-failed";
            default -> "domain-events"; // Tópico padrão
        };
    }
}

