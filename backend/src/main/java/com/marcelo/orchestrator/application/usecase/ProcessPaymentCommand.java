package com.marcelo.orchestrator.application.usecase;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Command para processamento de pagamento.
 * 
 * <p>Contém dados necessários para processar pagamento de um pedido.
 * Recebido da camada Presentation e validado antes de ser processado.</p>
 * 
 * @param orderId ID do pedido a ser pago
 * @param currency Moeda do pagamento (ex: "BRL", "USD")
 * @param paymentMethod Método de pagamento (ex: "CREDIT_CARD", "PIX", "DEBIT_CARD")
 * @author Marcelo
 */
@Getter
@Builder
public class ProcessPaymentCommand {
    
    private final UUID orderId;
    private final String currency;
    private final String paymentMethod;
}

