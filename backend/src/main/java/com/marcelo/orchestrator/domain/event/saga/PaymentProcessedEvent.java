package com.marcelo.orchestrator.domain.event.saga;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento publicado quando um pagamento é processado na saga.
 * 
 * <p>Implementa o padrão <strong>Domain Event</strong> e é publicado pelo
 * {@link com.marcelo.orchestrator.application.saga.OrderSagaOrchestrator}
 * após o Step 2 (Process Payment) ser concluído.</p>
 * 
 * <h3>Padrão: Domain Event</h3>
 * <p>Este evento pode ter dois resultados:</p>
 * <ul>
 *   <li><strong>SUCCESS:</strong> Pagamento aprovado (status: PAID)</li>
 *   <li><strong>FAILED:</strong> Pagamento rejeitado (status: PAYMENT_FAILED)</li>
 * </ul>
 * 
 * <h3>Consumidores Típicos:</h3>
 * <ul>
 *   <li><strong>Inventory Service:</strong> Confirma reserva de estoque se pagamento aprovado</li>
 *   <li><strong>Notification Service:</strong> Envia email de confirmação ou falha</li>
 *   <li><strong>Accounting Service:</strong> Registra transação financeira</li>
 *   <li><strong>Analytics Service:</strong> Atualiza métricas de pagamento</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class PaymentProcessedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final UUID aggregateId; // Order ID
    private final LocalDateTime occurredAt;
    private final String eventType = "PaymentProcessed";
    
    private final UUID orderId;
    private final String paymentStatus; // "PAID" ou "PAYMENT_FAILED"
    private final String paymentId; // ID do pagamento no gateway (pode ser null se falhou)
    private final java.math.BigDecimal amount;
    private final String currency;
    private final String failureReason; // null se sucesso
    private final UUID sagaId; // Pode ser null quando evento é publicado fora da saga
    
    /**
     * Cria evento a partir de um pedido e saga ID.
     * 
     * @param order Pedido
     * @param sagaId ID da saga (pode ser null se evento é publicado fora da saga)
     * @return PaymentProcessedEvent
     */
    public static PaymentProcessedEvent from(Order order, UUID sagaId) {
        return PaymentProcessedEvent.builder()
            .eventId(UUID.randomUUID())
            .aggregateId(order.getId())
            .occurredAt(LocalDateTime.now())
            .orderId(order.getId())
            .paymentStatus(order.getStatus().name())
            .paymentId(order.getPaymentId())
            .amount(order.getTotalAmount()) // BigDecimal direto do Order
            .currency("BRL") // Moeda padrão
            .failureReason(order.getStatus() == com.marcelo.orchestrator.domain.model.OrderStatus.PAYMENT_FAILED 
                ? "Payment rejected by gateway" : null)
            .sagaId(sagaId)
            .build();
    }
    
    /**
     * Cria evento a partir de um pedido sem saga ID.
     * 
     * <p>Útil quando o evento é publicado fora do contexto da saga,
     * por exemplo, quando o status é atualizado via refresh de status.</p>
     * 
     * @param order Pedido
     * @return PaymentProcessedEvent com sagaId = null
     */
    public static PaymentProcessedEvent fromOrder(Order order) {
        return from(order, null);
    }
    
    @Override
    public String getEventVersion() {
        return "1.0";
    }
}

