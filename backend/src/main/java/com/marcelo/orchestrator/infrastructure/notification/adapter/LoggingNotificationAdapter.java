package com.marcelo.orchestrator.infrastructure.notification.adapter;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.port.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class LoggingNotificationAdapter implements NotificationPort {
    
    @Override
    public void notifyOrderCreated(Order order) {
        log.info("📧 [NOTIFICATION] Order created - Order ID: {}, Number: {}, Customer: {} ({})", 
            order.getId(), 
            order.getOrderNumber(), 
            order.getCustomerName(),
            order.getCustomerEmail());
    }
    
    @Override
    public void notifyOrderStatusChanged(Order order) {
        log.info("📧 [NOTIFICATION] Order status changed - Order ID: {}, Number: {}, Status: {}", 
            order.getId(), 
            order.getOrderNumber(), 
            order.getStatus());
    }
    
    @Override
    public void notifyPaymentFailed(Order order) {
        log.warn("📧 [NOTIFICATION] Payment failed - Order ID: {}, Number: {}, Customer: {}", 
            order.getId(), 
            order.getOrderNumber(), 
            order.getCustomerEmail());
    }
    
    @Override
    public void notifyOrderCancelled(Order order) {
        log.info("📧 [NOTIFICATION] Order cancelled - Order ID: {}, Number: {}, Customer: {}", 
            order.getId(), 
            order.getOrderNumber(), 
            order.getCustomerEmail());
    }
}

