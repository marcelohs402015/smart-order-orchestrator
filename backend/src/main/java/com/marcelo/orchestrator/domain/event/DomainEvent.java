package com.marcelo.orchestrator.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interface base para todos os eventos de domínio.
 * 
 * <p>Implementa o padrão <strong>Domain Events</strong> do Domain-Driven Design (DDD),
 * onde eventos representam algo significativo que aconteceu no domínio.</p>
 * 
 * <h3>Padrão: Domain Events</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Eventos são imutáveis - representam algo que já aconteceu</li>
 *   <li><strong>Timestamp:</strong> Cada evento tem timestamp de quando ocorreu</li>
 *   <li><strong>Event ID:</strong> ID único para rastreamento e idempotência</li>
 *   <li><strong>Aggregate ID:</strong> ID do aggregate que gerou o evento (ex: Order ID)</li>
 * </ul>
 * 
 * <h3>Por que Domain Events?</h3>
 * <ul>
 *   <li><strong>Desacoplamento:</strong> Permite que diferentes partes do sistema reajam a eventos
 *       sem conhecer quem os publicou</li>
 *   <li><strong>Auditoria:</strong> Histórico completo de tudo que aconteceu</li>
 *   <li><strong>Integração:</strong> Facilita integração entre microserviços</li>
 *   <li><strong>Event Sourcing:</strong> Base para implementar Event Sourcing se necessário</li>
 * </ul>
 * 
 * <h3>Uso no Saga Pattern:</h3>
 * <p>Eventos são publicados em cada step da saga, permitindo que outros serviços
 * sejam notificados e reajam de forma assíncrona:</p>
 * <ul>
 *   <li>OrderCreatedEvent → Notification Service envia email</li>
 *   <li>PaymentProcessedEvent → Inventory Service reserva estoque</li>
 *   <li>SagaCompletedEvent → Analytics Service atualiza métricas</li>
 * </ul>
 * 
 * @author Marcelo
 */
public interface DomainEvent {
    
    /**
     * ID único do evento (para rastreamento e idempotência).
     * 
     * <p>Usado para garantir que o mesmo evento não seja processado duas vezes
     * (idempotência no consumidor).</p>
     */
    UUID getEventId();
    
    /**
     * ID do aggregate que gerou o evento (ex: Order ID).
     * 
     * <p>Permite correlacionar eventos do mesmo aggregate e manter ordem
     * de processamento quando necessário.</p>
     */
    UUID getAggregateId();
    
    /**
     * Timestamp de quando o evento ocorreu.
     * 
     * <p>Importante para auditoria e ordenação de eventos.</p>
     */
    LocalDateTime getOccurredAt();
    
    /**
     * Tipo do evento (ex: "OrderCreated", "PaymentProcessed").
     * 
     * <p>Usado pelo message broker para rotear eventos para tópicos/filas corretas.</p>
     */
    String getEventType();
    
    /**
     * Versão do evento (para evolução do schema).
     * 
     * <p>Permite que consumidores antigos e novos coexistam durante migrações.</p>
     */
    default String getEventVersion() {
        return "1.0";
    }
}

