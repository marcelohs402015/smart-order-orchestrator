package com.marcelo.orchestrator.presentation.service;

import com.marcelo.orchestrator.application.usecase.AnalyzeRiskCommand;
import com.marcelo.orchestrator.application.usecase.AnalyzeRiskUseCase;
import com.marcelo.orchestrator.application.usecase.RefreshPaymentStatusUseCase;
import com.marcelo.orchestrator.domain.event.saga.PaymentProcessedEvent;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.RiskLevel;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.domain.port.PaymentGatewayPort;
import com.marcelo.orchestrator.domain.port.PaymentStatus;
import com.marcelo.orchestrator.presentation.dto.OrderResponse;
import com.marcelo.orchestrator.presentation.dto.PaymentStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGatewayPort paymentGatewayPort;
    private final OrderRepositoryPort orderRepository;
    private final EventPublisherPort eventPublisher;
    private final AnalyzeRiskUseCase analyzeRiskUseCase;
    private final RefreshPaymentStatusUseCase refreshPaymentStatusUseCase;

    @Transactional
    public PaymentStatusResponse checkPaymentStatus(String paymentId) {
        log.info("Checking payment status for paymentId={}", paymentId);
        
        PaymentStatus gatewayStatus = paymentGatewayPort.checkPaymentStatus(paymentId);
        
        orderRepository.findByPaymentId(paymentId).ifPresent(order -> {
            PaymentStatus currentOrderStatus = mapOrderStatusToPaymentStatus(order.getStatus());
            
            if (currentOrderStatus != gatewayStatus) {
                log.info("Payment status changed for order {}. Current: {}, New: {}. Updating order.",
                    order.getId(), currentOrderStatus, gatewayStatus);
                
                boolean wasPaid = order.isPaid();
                
                updateOrderStatus(order, gatewayStatus);
                Order updatedOrder = orderRepository.save(order);
                
                if (!wasPaid && updatedOrder.isPaid()) {
                    publishPaymentProcessedEvent(updatedOrder);
                    triggerRiskAnalysisIfNeeded(updatedOrder);
                }
                
                log.info("Order {} updated with new payment status: {}", updatedOrder.getId(), gatewayStatus);
            } else {
                log.debug("Payment status unchanged for order {}. Status: {}", order.getId(), gatewayStatus);
            }
        });
        
        return new PaymentStatusResponse(paymentId, gatewayStatus);
    }

    @Transactional
    public OrderResponse refreshPaymentStatusForOrder(UUID orderId) {
        log.info("Refreshing payment status for order {}", orderId);
        Order updatedOrder = refreshPaymentStatusUseCase.execute(orderId);
        
        triggerRiskAnalysisIfNeeded(updatedOrder);
        
        return OrderResponse.from(updatedOrder);
    }

    private PaymentStatus mapOrderStatusToPaymentStatus(com.marcelo.orchestrator.domain.model.OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PAID -> PaymentStatus.SUCCESS;
            case PAYMENT_FAILED -> PaymentStatus.FAILED;
            case PAYMENT_PENDING -> PaymentStatus.PENDING;
            case CANCELED -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }

    private void updateOrderStatus(Order order, PaymentStatus paymentStatus) {
        switch (paymentStatus) {
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
                log.debug("Payment status is {} for order {}. Keeping current order status {}.",
                    paymentStatus, order.getId(), order.getStatus());
            }
        }
    }

    private void publishPaymentProcessedEvent(Order order) {
        try {
            PaymentProcessedEvent event = PaymentProcessedEvent.fromOrder(order);
            eventPublisher.publish(event);
            log.info("PaymentProcessedEvent published for order {} (status: PAID)", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentProcessedEvent for order {}: {}", 
                order.getId(), e.getMessage(), e);
        }
    }

    private void triggerRiskAnalysisIfNeeded(Order order) {
        if (order == null) {
            return;
        }
        
        if (order.isPaid() && order.getRiskLevel() == RiskLevel.PENDING) {
            try {
                log.info("Triggering risk analysis after payment confirmation for order {}", order.getId());
                AnalyzeRiskCommand command = AnalyzeRiskCommand.builder()
                    .orderId(order.getId())
                    .paymentMethod("PIX")
                    .build();
                analyzeRiskUseCase.execute(command);
            } catch (Exception e) {
                log.warn("Failed to analyze risk after payment confirmation for order {}: {}", 
                    order.getId(), e.getMessage());
            }
        } else {
            log.debug("Risk analysis not triggered for order {}. Status: {}, RiskLevel: {}", 
                order.getId(), order.getStatus(), order.getRiskLevel());
        }
    }
}