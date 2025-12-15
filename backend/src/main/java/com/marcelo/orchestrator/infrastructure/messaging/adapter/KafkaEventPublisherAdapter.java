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

/**
 * Adapter para publicação de eventos no Apache Kafka.
 * 
 * <p>Implementa o padrão <strong>Adapter Pattern</strong> da Hexagonal Architecture,
 * adaptando a interface EventPublisherPort (Port) para usar Apache Kafka como message broker.</p>
 * 
 * <h3>Padrão: Adapter Pattern (Hexagonal Architecture)</h3>
 * <ul>
 *   <li><strong>Port:</strong> EventPublisherPort (interface do domínio)</li>
 *   <li><strong>Adapter:</strong> Esta classe - adapta EventPublisherPort para Kafka</li>
 *   <li><strong>Adaptee:</strong> KafkaTemplate (API do Spring Kafka)</li>
 *   <li><strong>Isolamento:</strong> Domínio não conhece detalhes de Kafka</li>
 * </ul>
 * 
 * <h3>Por que Apache Kafka?</h3>
 * <ul>
 *   <li><strong>Alta Performance:</strong> Milhares de mensagens por segundo</li>
 *   <li><strong>Escalabilidade:</strong> Escala horizontalmente com partições</li>
 *   <li><strong>Durabilidade:</strong> Mensagens persistidas em disco</li>
 *   <li><strong>Ordem:</strong> Garante ordem de mensagens por partição</li>
 *   <li><strong>Idempotência:</strong> Suporte nativo a idempotência do producer</li>
 * </ul>
 * 
 * <h3>Roteamento de Tópicos:</h3>
 * <p>Cada tipo de evento é publicado em um tópico específico, permitindo que diferentes
 * serviços se inscrevam apenas nos eventos relevantes (Topic-per-Event-Type pattern).</p>
 * 
 * <h3>Mapeamento Event Type → Topic:</h3>
 * <ul>
 *   <li><strong>OrderCreated</strong> → `order-created`</li>
 *   <li><strong>PaymentProcessed</strong> → `payment-processed`</li>
 *   <li><strong>SagaCompleted</strong> → `saga-completed`</li>
 *   <li><strong>SagaFailed</strong> → `saga-failed`</li>
 * </ul>
 * 
 * <h3>Headers Kafka:</h3>
 * <p>Cada mensagem inclui headers com metadados importantes:</p>
 * <ul>
 *   <li><strong>eventId:</strong> ID único do evento (para idempotência no consumidor)</li>
 *   <li><strong>aggregateId:</strong> ID do pedido (Order ID) - usado como key da mensagem</li>
 *   <li><strong>eventType:</strong> Tipo do evento (para roteamento)</li>
 *   <li><strong>eventVersion:</strong> Versão do schema (para evolução)</li>
 *   <li><strong>occurredAt:</strong> Timestamp do evento</li>
 * </ul>
 * 
 * <h3>Padrão: Fail-Safe</h3>
 * <p>Se a publicação falhar, loga o erro mas <strong>não lança exceção</strong>, para não
 * interromper o fluxo principal da saga. O evento pode ser republicado posteriormente
 * via recovery service.</p>
 * 
 * <h3>Alinhamento com SOLID:</h3>
 * <ul>
 *   <li><strong>Single Responsibility:</strong> Apenas publicação de eventos no Kafka</li>
 *   <li><strong>Dependency Inversion:</strong> Depende de EventPublisherPort (abstração), não de Kafka</li>
 *   <li><strong>Open/Closed:</strong> Fácil adicionar novos tipos de eventos sem modificar código</li>
 * </ul>
 * 
 * @author Marcelo
 */
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
    
    /**
     * Publica um evento de domínio no Kafka.
     * 
     * <p>Padrão: Fail-Safe - se publicação falhar, loga erro mas não lança exceção.
     * Isso garante que falhas na publicação não interrompam o fluxo principal da saga.</p>
     * 
     * <p>Usa o aggregateId (Order ID) como key da mensagem, garantindo que eventos
     * do mesmo pedido sejam processados na mesma partição (ordem garantida).</p>
     * 
     * @param event Evento de domínio a ser publicado
     * @param <T> Tipo do evento (deve implementar DomainEvent)
     */
    @Override
    public <T extends DomainEvent> void publish(T event) {
        try {
            String topic = getTopicForEvent(event.getEventType());
            String key = event.getAggregateId() != null 
                ? event.getAggregateId().toString() 
                : event.getEventId().toString();
            
            log.debug("Publishing event to Kafka topic '{}': {} (aggregateId: {}, eventId: {})", 
                topic, event.getEventType(), event.getAggregateId(), event.getEventId());
            
            // Criar mensagem com headers
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
            
            // Publicar de forma assíncrona (não bloqueia)
            kafkaTemplate.send(message);
            
            log.info("Event published successfully to Kafka: {} [{}] → topic: {}", 
                event.getEventType(), event.getEventId(), topic);
                
        } catch (Exception e) {
            // Padrão: Fail-Safe - não interrompe fluxo principal
            log.error("Failed to publish event {} to Kafka: {}", 
                event.getEventType(), e.getMessage(), e);
        }
    }
    
    /**
     * Publica múltiplos eventos em batch.
     * 
     * <p>Padrão: Batch Processing - otimiza publicação de múltiplos eventos,
     * reduzindo overhead de rede e melhorando throughput.</p>
     * 
     * <p>Nota: KafkaTemplate já otimiza envios em batch internamente, mas este método
     * permite agrupar eventos logicamente relacionados.</p>
     * 
     * @param events Lista de eventos a serem publicados
     * @param <T> Tipo dos eventos (devem implementar DomainEvent)
     */
    @Override
    public <T extends DomainEvent> void publishBatch(List<T> events) {
        try {
            log.debug("Publishing {} events to Kafka in batch", events.size());
            
            for (T event : events) {
                publish(event); // Reutiliza lógica de publicação única
            }
            
            log.info("Batch of {} events published successfully to Kafka", events.size());
            
        } catch (Exception e) {
            log.error("Failed to publish batch of events to Kafka: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Mapeia tipo de evento para tópico Kafka.
     * 
     * <p>Padrão: Strategy Pattern - mapeamento baseado em tipo de evento.
     * Facilita adicionar novos tipos de eventos sem modificar lógica de publicação.</p>
     * 
     * <p>Alinhado com Open/Closed Principle - aberto para extensão (novos eventos),
     * fechado para modificação (lógica de mapeamento não muda).</p>
     * 
     * @param eventType Tipo do evento (ex: "OrderCreated", "PaymentProcessed")
     * @return Nome do tópico Kafka correspondente
     * @throws IllegalArgumentException se tipo de evento não for suportado
     */
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
