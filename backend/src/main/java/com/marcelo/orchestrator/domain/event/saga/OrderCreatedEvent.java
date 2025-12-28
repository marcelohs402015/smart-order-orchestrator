package com.marcelo.orchestrator.domain.event.saga;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

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

