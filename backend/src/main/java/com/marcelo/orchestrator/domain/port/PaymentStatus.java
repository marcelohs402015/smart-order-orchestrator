package com.marcelo.orchestrator.domain.port;

/**
 * Enum que representa os possíveis status de um pagamento no gateway.
 * 
 * <p>Utilizado para comunicar o estado do pagamento entre a camada de domínio
 * e a implementação do gateway de pagamento.</p>
 * 
 * @author Marcelo
 */
public enum PaymentStatus {
    
    /**
     * Pagamento pendente - aguardando processamento.
     */
    PENDING,
    
    /**
     * Pagamento processado com sucesso.
     */
    SUCCESS,
    
    /**
     * Pagamento falhou (cartão recusado, saldo insuficiente, etc.).
     */
    FAILED,
    
    /**
     * Pagamento foi reembolsado.
     */
    REFUNDED,
    
    /**
     * Pagamento foi cancelado antes do processamento.
     */
    CANCELLED
}

