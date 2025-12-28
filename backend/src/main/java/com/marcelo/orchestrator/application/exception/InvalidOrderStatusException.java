package com.marcelo.orchestrator.application.exception;

import com.marcelo.orchestrator.domain.model.OrderStatus;

public class InvalidOrderStatusException extends DomainException {
    
    public InvalidOrderStatusException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super(String.format(
            "Cannot transition from %s to %s. Allowed transitions from %s: %s",
            currentStatus, targetStatus, currentStatus, currentStatus.getAllowedTransitions()
        ));
    }
}

