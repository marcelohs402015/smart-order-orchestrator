package com.marcelo.orchestrator.application.exception;

import java.util.UUID;

public class OrderNotFoundException extends DomainException {
    
    public OrderNotFoundException(UUID orderId) {
        super(String.format("Order not found: %s", orderId));
    }
    
    public OrderNotFoundException(String orderNumber) {
        super(String.format("Order not found with number: %s", orderNumber));
    }
    
    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

