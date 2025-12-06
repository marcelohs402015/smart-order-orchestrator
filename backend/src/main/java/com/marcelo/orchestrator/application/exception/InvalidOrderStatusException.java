package com.marcelo.orchestrator.application.exception;

import com.marcelo.orchestrator.domain.model.OrderStatus;

/**
 * Exceção lançada quando uma transição de status não é permitida.
 * 
 * <p>Representa violação de regra de negócio: tentativa de transicionar
 * para um status que não é permitido pelo State Machine.</p>
 * 
 * @author Marcelo
 */
public class InvalidOrderStatusException extends DomainException {
    
    public InvalidOrderStatusException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super(String.format(
            "Cannot transition from %s to %s. Allowed transitions from %s: %s",
            currentStatus, targetStatus, currentStatus, currentStatus.getAllowedTransitions()
        ));
    }
}

