package com.marcelo.orchestrator.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Record que representa uma requisição de pagamento.
 * 
 * <p>Utiliza Java Record para imutabilidade e simplicidade.
 * Records são ideais para DTOs e Value Objects - menos boilerplate,
 * imutáveis por padrão, e geram equals/hashCode/toString automaticamente.</p>
 * 
 * <h3>Por que Record?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Dados não podem ser alterados após criação</li>
 *   <li><strong>Simplicidade:</strong> Menos código, mais legível</li>
 *   <li><strong>Performance:</strong> Menos overhead que classes tradicionais</li>
 *   <li><strong>Thread-Safe:</strong> Imutabilidade garante segurança em concorrência</li>
 * </ul>
 * 
 * @param orderId ID do pedido associado ao pagamento
 * @param amount Valor a ser pago
 * @param currency Moeda (ex: "BRL", "USD")
 * @param paymentMethod Método de pagamento (ex: "CREDIT_CARD", "PIX")
 * @param customerEmail Email do cliente (para notificações)
 * @author Marcelo
 */
public record PaymentRequest(
    UUID orderId,
    BigDecimal amount,
    String currency,
    String paymentMethod,
    String customerEmail
) {
    /**
     * Validação básica do record.
     * 
     * @throws IllegalArgumentException se dados inválidos
     */
    public PaymentRequest {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be null or blank");
        }
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method cannot be null or blank");
        }
    }
}

