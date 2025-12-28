package com.marcelo.orchestrator.domain.model;

import java.util.Set;

public enum OrderStatus {
    
    PENDING,

    PAYMENT_PENDING,

    PAID,

    PAYMENT_FAILED,

    CANCELED;

    public Set<OrderStatus> getAllowedTransitions() {
        return switch (this) {
            
            case PENDING -> Set.of(PAYMENT_PENDING, PAID, PAYMENT_FAILED, CANCELED);
            case PAYMENT_PENDING -> Set.of(PAID, PAYMENT_FAILED);
            case PAYMENT_FAILED -> Set.of(CANCELED); 
            case PAID, CANCELED -> Set.of(); 
        };
    }

    public boolean canTransitionTo(OrderStatus targetStatus) {
        return getAllowedTransitions().contains(targetStatus);
    }
    
}

