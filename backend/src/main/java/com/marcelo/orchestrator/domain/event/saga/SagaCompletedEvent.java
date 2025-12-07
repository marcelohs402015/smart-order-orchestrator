package com.marcelo.orchestrator.domain.event.saga;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento publicado quando a saga completa com sucesso.
 * 
 * <p>Implementa o padrão <strong>Domain Event</strong> e é publicado pelo
 * {@link com.marcelo.orchestrator.application.saga.OrderSagaOrchestrator}
 * quando todos os 3 steps são concluídos com sucesso.</p>
 * 
 * <h3>Padrão: Domain Event</h3>
 * <p>Este é o evento final da saga, indicando que todo o processo
 * foi concluído com sucesso e o pedido está pronto para processamento.</p>
 * 
 * <h3>Consumidores Típicos:</h3>
 * <ul>
 *   <li><strong>Fulfillment Service:</strong> Inicia processo de preparação/envio</li>
 *   <li><strong>Notification Service:</strong> Envia email final de confirmação</li>
 *   <li><strong>Analytics Service:</strong> Registra métrica de saga completa</li>
 *   <li><strong>Dashboard Service:</strong> Atualiza dashboard em tempo real</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class SagaCompletedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final UUID aggregateId; // Order ID
    private final LocalDateTime occurredAt;
    private final String eventType = "SagaCompleted";
    
    private final UUID orderId;
    private final UUID sagaId;
    private final String orderStatus;
    private final String riskLevel;
    private final Long durationMs; // Duração total da saga em milissegundos
    
    public static SagaCompletedEvent from(Order order, UUID sagaId, Long durationMs) {
        return SagaCompletedEvent.builder()
            .eventId(UUID.randomUUID())
            .aggregateId(order.getId())
            .occurredAt(LocalDateTime.now())
            .orderId(order.getId())
            .sagaId(sagaId)
            .orderStatus(order.getStatus().name())
            .riskLevel(order.getRiskLevel() != null ? order.getRiskLevel().name() : "PENDING")
            .durationMs(durationMs)
            .build();
    }
    
    @Override
    public String getEventVersion() {
        return "1.0";
    }
}

