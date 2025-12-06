package com.marcelo.orchestrator.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Record que representa uma requisição de análise de risco.
 * 
 * <p>Contém metadados do pedido e contexto do cliente que serão enviados
 * para o serviço de IA para análise semântica de risco.</p>
 * 
 * <h3>Dados Enviados para IA:</h3>
 * <ul>
 *   <li>Informações do pedido (valor, itens)</li>
 *   <li>Histórico do cliente (se disponível)</li>
 *   <li>Contexto adicional (método de pagamento, localização, etc.)</li>
 * </ul>
 * 
 * @param orderId ID do pedido
 * @param orderAmount Valor total do pedido
 * @param customerId ID do cliente
 * @param customerEmail Email do cliente
 * @param paymentMethod Método de pagamento utilizado
 * @param additionalContext Contexto adicional em formato texto (para análise semântica)
 * @author Marcelo
 */
public record RiskAnalysisRequest(
    UUID orderId,
    BigDecimal orderAmount,
    UUID customerId,
    String customerEmail,
    String paymentMethod,
    String additionalContext
) {
    /**
     * Validação básica do record.
     * 
     * @throws IllegalArgumentException se dados inválidos
     */
    public RiskAnalysisRequest {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order amount must be greater than zero");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
    }
}

