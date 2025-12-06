package com.marcelo.orchestrator.domain.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record que representa o resultado de um processamento de pagamento.
 * 
 * <p>Utiliza Java Record para imutabilidade. Contém informações sobre o sucesso
 * ou falha do processamento, incluindo ID do pagamento no gateway externo.</p>
 * 
 * <h3>Estrutura do Resultado:</h3>
 * <ul>
 *   <li><strong>paymentId:</strong> ID gerado pelo gateway (null se falhou)</li>
 *   <li><strong>status:</strong> Status do pagamento (SUCCESS, FAILED, PENDING)</li>
 *   <li><strong>message:</strong> Mensagem descritiva (erro ou confirmação)</li>
 *   <li><strong>amount:</strong> Valor processado</li>
 *   <li><strong>processedAt:</strong> Timestamp do processamento</li>
 * </ul>
 * 
 * @param paymentId ID do pagamento no gateway (null se falhou)
 * @param status Status do processamento
 * @param message Mensagem descritiva
 * @param amount Valor processado
 * @param processedAt Timestamp do processamento
 * @author Marcelo
 */
public record PaymentResult(
    String paymentId,
    PaymentStatus status,
    String message,
    BigDecimal amount,
    LocalDateTime processedAt
) {
    /**
     * Verifica se o pagamento foi processado com sucesso.
     * 
     * @return {@code true} se status é SUCCESS, {@code false} caso contrário
     */
    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }
    
    /**
     * Verifica se o pagamento falhou.
     * 
     * @return {@code true} se status é FAILED, {@code false} caso contrário
     */
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }
}

