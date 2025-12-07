package com.marcelo.orchestrator.infrastructure.messaging.adapter;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter para publicação de eventos no Google Cloud Pub/Sub.
 * 
 * <p>Implementa o padrão <strong>Adapter Pattern</strong> da Hexagonal Architecture,
 * adaptando a interface EventPublisherPort para usar Google Cloud Pub/Sub como message broker.</p>
 * 
 * <h3>Padrão: Adapter Pattern</h3>
 * <ul>
 *   <li><strong>Adapter:</strong> Esta classe - adapta EventPublisherPort para Pub/Sub</li>
 *   <li><strong>Target:</strong> EventPublisherPort (interface do domínio)</li>
 *   <li><strong>Adaptee:</strong> PubSubTemplate (API do Spring Cloud GCP)</li>
 * </ul>
 * 
 * <h3>Por que Google Cloud Pub/Sub?</h3>
 * <ul>
 *   <li><strong>Serverless:</strong> Gerenciado pelo GCP, sem infraestrutura para manter</li>
 *   <li><strong>Escalabilidade Automática:</strong> Escala automaticamente com carga</li>
 *   <li><strong>Alta Disponibilidade:</strong> 99.95% SLA garantido</li>
 *   <li><strong>At-least-once Delivery:</strong> Garante entrega de mensagens</li>
 *   <li><strong>Integração GCP:</strong> Integra nativamente com outros serviços GCP</li>
 * </ul>
 * 
 * <h3>Configuração:</h3>
 * <pre>{@code
 * # application.yml
 * app:
 *   message:
 *     broker:
 *       type: PUBSUB
 * 
 * spring:
 *   cloud:
 *     gcp:
 *       project-id: my-project-id
 *       credentials:
 *         location: classpath:gcp-credentials.json
 *     pubsub:
 *       topic:
 *         order-created: order-created-topic
 *         payment-processed: payment-processed-topic
 *         saga-completed: saga-completed-topic
 *         saga-failed: saga-failed-topic
 * }</pre>
 * 
 * <h3>Roteamento de Tópicos:</h3>
 * <p>Cada tipo de evento é publicado em um tópico específico do Pub/Sub,
 * permitindo que diferentes serviços se inscrevam apenas nos eventos relevantes.</p>
 * 
 * <h3>Padrão: Topic-per-Event-Type</h3>
 * <p>Mesma estratégia do Kafka - cada tipo de evento tem seu próprio tópico.</p>
 * 
 * @author Marcelo
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.message.broker.type", havingValue = "PUBSUB")
public class PubSubEventPublisherAdapter implements EventPublisherPort {
    
    // TODO: Injetar PubSubTemplate quando dependência for adicionada
    // private final PubSubTemplate pubSubTemplate;
    
    /**
     * Construtor (placeholder para quando PubSubTemplate for adicionado).
     * 
     * <p>Por enquanto, esta implementação está preparada mas não funcional.
     * Para ativar, adicionar dependência do Spring Cloud GCP Pub/Sub:</p>
     * <pre>{@code
     * <dependency>
     *     <groupId>com.google.cloud</groupId>
     *     <artifactId>spring-cloud-gcp-starter-pubsub</artifactId>
     * </dependency>
     * }</pre>
     */
    public PubSubEventPublisherAdapter() {
        log.warn("PubSubEventPublisherAdapter created but not fully implemented. " +
                "Add Spring Cloud GCP Pub/Sub dependency to enable.");
    }
    
    /**
     * Publica evento no Pub/Sub.
     * 
     * <p>Padrão: Fail-Safe - se publicação falhar, loga erro mas não lança exceção.</p>
     */
    @Override
    public <T extends DomainEvent> void publish(T event) {
        try {
            String topic = getTopicForEvent(event);
            
            log.debug("Publishing event to Pub/Sub topic '{}': {} (aggregateId: {})", 
                topic, event.getEventType(), event.getAggregateId());
            
            // TODO: Implementar quando PubSubTemplate estiver disponível
            // pubSubTemplate.publish(topic, event);
            
            log.warn("Pub/Sub publishing not yet implemented. Event {} would be published to topic '{}'", 
                event.getEventType(), topic);
                
        } catch (Exception e) {
            log.error("Failed to publish event {} to Pub/Sub: {}", 
                event.getEventType(), e.getMessage(), e);
        }
    }
    
    /**
     * Publica múltiplos eventos em batch.
     */
    @Override
    public <T extends DomainEvent> void publishBatch(List<T> events) {
        try {
            log.debug("Publishing {} events to Pub/Sub in batch", events.size());
            
            // TODO: Implementar batch publishing
            events.forEach(this::publish);
            
        } catch (Exception e) {
            log.error("Failed to publish batch to Pub/Sub: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Mapeia tipo de evento para tópico Pub/Sub.
     */
    private String getTopicForEvent(DomainEvent event) {
        return switch (event.getEventType()) {
            case "OrderCreated" -> "order-created";
            case "PaymentProcessed" -> "payment-processed";
            case "SagaCompleted" -> "saga-completed";
            case "SagaFailed" -> "saga-failed";
            default -> "domain-events";
        };
    }
}

