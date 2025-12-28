package com.marcelo.orchestrator.domain.event.saga;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentProcessedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final UUID aggregateId; 
    private final LocalDateTime occurredAt;
    private final String eventType = "PaymentProcessed";
    
    private final UUID orderId;
    private final String paymentStatus; 
    private final String paymentId; 
    private final java.math.BigDecimal amount;
    private final String currency;
    private final String failureReason; 
    private final UUID sagaId; 

    public static PaymentProcessedEvent from(Order order, UUID sagaId) {
        return PaymentProcessedEvent.builder()
            .eventId(UUID.randomUUID())
            .aggregateId(order.getId())
            .occurredAt(LocalDateTime.now())
            .orderId(order.getId())
            .paymentStatus(order.getStatus().name())
            .paymentId(order.getPaymentId())
            .amount(order.getTotalAmount()) 
            .currency("BRL") 
            .failureReason(order.getStatus() == com.marcelo.orchestrator.domain.model.OrderStatus.PAYMENT_FAILED 
                ? "Payment rejected by gateway" : null)
            .sagaId(sagaId)
            .build();
    }
    
    public static PaymentProcessedEvent fromOrder(Order order) {
        return from(order, null);
    }
    
    @Override
    public String getEventVersion() {
        return "1.0";
    }
}

