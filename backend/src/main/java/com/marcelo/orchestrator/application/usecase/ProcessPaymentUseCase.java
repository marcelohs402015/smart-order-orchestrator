package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.port.NotificationPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.application.exception.OrderNotFoundException;
import com.marcelo.orchestrator.domain.port.PaymentGatewayPort;
import com.marcelo.orchestrator.domain.port.PaymentRequest;
import com.marcelo.orchestrator.domain.port.PaymentResult;
import com.marcelo.orchestrator.domain.port.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {
    
    private final OrderRepositoryPort orderRepository;
    private final PaymentGatewayPort paymentGateway;
    private final NotificationPort notificationPort;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order execute(ProcessPaymentCommand command) {
        log.info("Processing payment for order: {}", command.getOrderId());
        
        
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));
        
        
        if (!order.isPending()) {
            throw new IllegalStateException(
                String.format("Order %s is not in PENDING status. Current status: %s",
                    order.getId(), order.getStatus())
            );
        }
        
        
        PaymentRequest paymentRequest = new PaymentRequest(
            order.getId(),
            order.getTotalAmount(),
            command.getCurrency(),
            command.getPaymentMethod(),
            order.getCustomerEmail()
        );
        
        
        PaymentResult paymentResult = paymentGateway.processPayment(paymentRequest);
        
        
        if (paymentResult.isSuccessful()) {
            
            order.markAsPaid(paymentResult.paymentId());
            log.info("Payment successful for order: {} - Payment ID: {}",
                order.getId(), paymentResult.paymentId());
        } else {
            
            
            
            if (paymentResult.status() == PaymentStatus.PENDING && paymentResult.paymentId() != null) {
                order.attachPaymentId(paymentResult.paymentId());
                order.updateStatus(com.marcelo.orchestrator.domain.model.OrderStatus.PAYMENT_PENDING);
                log.info("Payment pending for order: {} - Payment ID: {}",
                    order.getId(), paymentResult.paymentId());
            } else {
                order.markAsPaymentFailed();
                log.warn("Payment failed for order: {} - Reason: {}",
                    order.getId(), paymentResult.message());
            }
        }
        
        
        Order updatedOrder = orderRepository.save(order);
        
        
        try {
            if (paymentResult.isSuccessful()) {
                notificationPort.notifyOrderStatusChanged(updatedOrder);
            } else {
                notificationPort.notifyPaymentFailed(updatedOrder);
            }
        } catch (Exception e) {
            log.warn("Failed to send notification for order {}: {}", updatedOrder.getId(), e.getMessage());
        }
        
        return updatedOrder;
    }
}

