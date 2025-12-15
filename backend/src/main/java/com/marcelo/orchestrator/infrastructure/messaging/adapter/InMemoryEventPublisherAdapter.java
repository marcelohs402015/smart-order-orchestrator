package com.marcelo.orchestrator.infrastructure.messaging.adapter;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adapter in-memory para publicação de eventos (usado em testes e desenvolvimento).
 * 
 * <p>Implementa o padrão <strong>Adapter Pattern</strong> da Hexagonal Architecture,
 * fornecendo uma implementação simples de EventPublisherPort que armazena eventos
 * em memória ao invés de publicar em um message broker real.</p>
 * 
 * <h3>Padrão: Adapter Pattern</h3>
 * <ul>
 *   <li><strong>Adapter:</strong> Esta classe - adapta a interface EventPublisherPort</li>
 *   <li><strong>Target:</strong> EventPublisherPort (interface do domínio)</li>
 *   <li><strong>Adaptee:</strong> Lista em memória (simula message broker)</li>
 * </ul>
 * 
 * <h3>Por que In-Memory Adapter?</h3>
 * <ul>
 *   <li><strong>Testes:</strong> Permite testar lógica de publicação sem dependências externas</li>
 *   <li><strong>Desenvolvimento:</strong> Não precisa configurar Kafka/Pub/Sub localmente</li>
 *   <li><strong>Simplicidade:</strong> Para ambientes simples que não precisam de message broker</li>
 *   <li><strong>Performance:</strong> Mais rápido que message broker real (útil em testes)</li>
 * </ul>
 * 
 * <h3>Limitações:</h3>
 * <ul>
 *   <li><strong>Não Persistente:</strong> Eventos são perdidos se aplicação reiniciar</li>
 *   <li><strong>Não Distribuído:</strong> Não funciona em ambiente distribuído (múltiplas instâncias)</li>
 *   <li><strong>Sem Garantias:</strong> Não garante at-least-once delivery</li>
 * </ul>
 * 
 * <h3>Para Testes:</h3>
 * <pre>{@code
 * // Em testes, pode verificar eventos publicados:
 * InMemoryEventPublisherAdapter adapter = ...;
 * List<DomainEvent> events = adapter.getPublishedEvents();
 * assertThat(events).hasSize(1);
 * assertThat(events.get(0)).isInstanceOf(OrderCreatedEvent.class);
 * }</pre>
 * 
 * @author Marcelo
 */
@Slf4j
@Component
public class InMemoryEventPublisherAdapter implements EventPublisherPort {
    
    /**
     * Lista thread-safe de eventos publicados.
     * 
     * <p>Usa CopyOnWriteArrayList para garantir thread-safety em ambientes
     * concorrentes (múltiplas threads publicando eventos simultaneamente).</p>
     * 
     * <p>Padrão: Thread-Safe Collection - garante que múltiplas threads
     * possam ler/escrever sem race conditions.</p>
     */
    private final List<DomainEvent> publishedEvents = new CopyOnWriteArrayList<>();
    
    /**
     * Publica um evento em memória.
     * 
     * <p>Padrão: Fail-Safe - nunca lança exceção, apenas loga erros.
     * Isso garante que falhas na publicação não interrompam o fluxo principal.</p>
     */
    @Override
    public <T extends DomainEvent> void publish(T event) {
        try {
            log.debug("Publishing event in-memory: {} (aggregateId: {})", 
                event.getEventType(), event.getAggregateId());
            
            publishedEvents.add(event);
            
            log.info("Event published successfully: {} [{}]", 
                event.getEventType(), event.getEventId());
                
        } catch (Exception e) {
            // Padrão: Fail-Safe - não interrompe fluxo principal
            log.error("Failed to publish event {}: {}", event.getEventType(), e.getMessage(), e);
        }
    }
    
    /**
     * Publica múltiplos eventos em batch.
     * 
     * <p>Padrão: Batch Processing - otimiza publicação de múltiplos eventos,
     * reduzindo overhead.</p>
     */
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
    
    /**
     * Retorna lista de eventos publicados (útil para testes).
     * 
     * <p>Método de conveniência para testes e debugging.</p>
     */
    public List<DomainEvent> getPublishedEvents() {
        return new ArrayList<>(publishedEvents);
    }
    
    /**
     * Limpa eventos publicados (útil para testes).
     */
    public void clear() {
        publishedEvents.clear();
        log.debug("Cleared published events");
    }
    
    /**
     * Conta eventos publicados por tipo.
     */
    public long countByType(String eventType) {
        return publishedEvents.stream()
            .filter(event -> event.getEventType().equals(eventType))
            .count();
    }
}

