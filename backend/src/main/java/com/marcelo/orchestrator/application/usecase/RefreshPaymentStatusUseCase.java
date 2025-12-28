package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.event.saga.PaymentProcessedEvent;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.domain.port.PaymentGatewayPort;
import com.marcelo.orchestrator.domain.port.PaymentStatus;
import com.marcelo.orchestrator.application.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshPaymentStatusUseCase {

    private final OrderRepositoryPort orderRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final EventPublisherPort eventPublisher;

    public Order execute(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getPaymentId() == null || order.getPaymentId().isBlank()) {
            log.warn("Order {} does not have a paymentId associated. Skipping payment status refresh.", orderId);
            return order;
        }

        PaymentStatus status = paymentGatewayPort.checkPaymentStatus(order.getPaymentId());
        log.info("Refreshed payment status for order {}. paymentId={}, status={}",
            orderId, order.getPaymentId(), status);

        // Salvar status anterior para verificar se mudou para PAID
        boolean wasPaid = order.isPaid();

        switch (status) {
            case SUCCESS -> {
                if (!order.isPaid()) {
                    order.markAsPaid(order.getPaymentId());
                }
            }
            case FAILED, CANCELLED -> {
                if (!order.isPaymentFailed() && !order.isCanceled()) {
                    order.markAsPaymentFailed();
                }
            }
            case PENDING, REFUNDED -> {
                // For now, keep current order status; could be extended later.
                log.info("Payment status is {} for order {}. Keeping current order status {}.",
                    status, orderId, order.getStatus());
            }
        }

        Order updatedOrder = orderRepository.save(order);
        
        // Publicar evento no Kafka se status mudou para PAID
        if (!wasPaid && updatedOrder.isPaid()) {
            publishPaymentProcessedEvent(updatedOrder);
        }
        
        return updatedOrder;
    }
    
    private void publishPaymentProcessedEvent(Order order) {
        try {
            PaymentProcessedEvent event = PaymentProcessedEvent.fromOrder(order);
            eventPublisher.publish(event);
            log.info("PaymentProcessedEvent published for order {} (status: PAID)", order.getId());
        } catch (Exception e) {
            // Padrão: Fail-Safe - loga erro mas não lança exceção
            log.error("Failed to publish PaymentProcessedEvent for order {}: {}", 
                order.getId(), e.getMessage(), e);
        }
    }
}


