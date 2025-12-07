package com.marcelo.orchestrator.domain.event.saga;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento publicado quando um pedido é criado na saga.
 * 
 * <p>Implementa o padrão <strong>Domain Event</strong> e é publicado pelo
 * {@link com.marcelo.orchestrator.application.saga.OrderSagaOrchestrator}
 * após o Step 1 (Create Order) ser concluído com sucesso.</p>
 * 
 * <h3>Padrão: Domain Event</h3>
 * <ul>
 *   <li><strong>Imutável:</strong> Representa algo que já aconteceu</li>
 *   <li><strong>Rastreável:</strong> Tem eventId único para idempotência</li>
 *   <li><strong>Timestamp:</strong> Registra quando o evento ocorreu</li>
 * </ul>
 * 
 * <h3>Consumidores Típicos:</h3>
 * <ul>
 *   <li><strong>Notification Service:</strong> Envia email de confirmação ao cliente</li>
 *   <li><strong>Inventory Service:</strong> Reserva estoque dos produtos</li>
 *   <li><strong>Analytics Service:</strong> Registra métrica de novo pedido</li>
 *   <li><strong>Audit Service:</strong> Registra evento para auditoria</li>
 * </ul>
 * 
 * <h3>Uso no Event-Driven Architecture:</h3>
 * <p>Este evento permite que outros serviços reajam à criação do pedido
 * de forma assíncrona e desacoplada, sem que o Saga Orchestrator precise
 * conhecer esses serviços.</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class OrderCreatedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final UUID aggregateId; // Order ID
    private final LocalDateTime occurredAt;
    private final String eventType = "OrderCreated";
    
    // Dados do pedido (snapshot no momento da criação)
    private final UUID orderId;
    private final String orderNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final java.math.BigDecimal totalAmount; // Valor total do pedido
    private final String currency; // Moeda (ex: "BRL")
    private final UUID sagaId; // ID da execução da saga
    
    /**
     * Factory method para criar evento a partir de uma Order.
     * 
     * <p>Padrão: Factory Method - encapsula a criação do evento,
     * garantindo que todos os campos necessários sejam preenchidos.</p>
     */
    public static OrderCreatedEvent from(Order order, UUID sagaId) {
        return OrderCreatedEvent.builder()
            .eventId(UUID.randomUUID())
            .aggregateId(order.getId())
            .occurredAt(LocalDateTime.now())
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber())
            .customerId(order.getCustomerId())
            .customerName(order.getCustomerName())
            .customerEmail(order.getCustomerEmail())
            .totalAmount(order.getTotalAmount()) // BigDecimal direto do Order
            .currency("BRL") // Moeda padrão (pode ser configurável no futuro)
            .sagaId(sagaId)
            .build();
    }
    
    @Override
    public String getEventVersion() {
        return "1.0";
    }
}

