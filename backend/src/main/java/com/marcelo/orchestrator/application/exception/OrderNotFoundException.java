package com.marcelo.orchestrator.application.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um pedido não é encontrado.
 * 
 * <p>Representa um erro de negócio: tentativa de operar em um pedido que não existe.
 * Diferente de erros técnicos (ex: erro de conexão com banco).</p>
 * 
 * @author Marcelo
 */
public class OrderNotFoundException extends DomainException {
    
    public OrderNotFoundException(UUID orderId) {
        super(String.format("Order not found: %s", orderId));
    }
    
    public OrderNotFoundException(String orderNumber) {
        super(String.format("Order not found with number: %s", orderNumber));
    }
}

