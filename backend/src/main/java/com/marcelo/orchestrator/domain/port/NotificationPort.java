package com.marcelo.orchestrator.domain.port;

import com.marcelo.orchestrator.domain.model.Order;

public interface NotificationPort {
    
    void notifyOrderCreated(Order order);
    
    void notifyOrderStatusChanged(Order order);
    
    void notifyPaymentFailed(Order order);
    
    void notifyOrderCancelled(Order order);
}

