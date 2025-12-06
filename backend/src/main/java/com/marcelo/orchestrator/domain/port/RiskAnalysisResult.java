package com.marcelo.orchestrator.domain.port;

import com.marcelo.orchestrator.domain.model.RiskLevel;

import java.time.LocalDateTime;

/**
 * Record que representa o resultado de uma análise de risco.
 * 
 * <p>Contém o nível de risco determinado pela IA e informações adicionais
 * sobre a análise (confiança, razão, etc.).</p>
 * 
 * <h3>Níveis de Risco:</h3>
 * <ul>
 *   <li><strong>LOW:</strong> Pedido considerado seguro, processamento normal</li>
 *   <li><strong>HIGH:</strong> Pedido com indicadores de risco, requer revisão</li>
 *   <li><strong>PENDING:</strong> Análise não concluída ou indisponível</li>
 * </ul>
 * 
 * @param riskLevel Nível de risco determinado (LOW, HIGH, PENDING)
 * @param confidenceScore Score de confiança da análise (0.0 a 1.0, opcional)
 * @param reason Razão da classificação (explicação da IA, opcional)
 * @param analyzedAt Timestamp da análise
 * @author Marcelo
 */
public record RiskAnalysisResult(
    RiskLevel riskLevel,
    Double confidenceScore,
    String reason,
    LocalDateTime analyzedAt
) {
    /**
     * Verifica se o risco é baixo (pedido seguro).
     * 
     * @return {@code true} se riskLevel é LOW, {@code false} caso contrário
     */
    public boolean isLowRisk() {
        return riskLevel == RiskLevel.LOW;
    }
    
    /**
     * Verifica se o risco é alto (requer atenção).
     * 
     * @return {@code true} se riskLevel é HIGH, {@code false} caso contrário
     */
    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH;
    }
}

