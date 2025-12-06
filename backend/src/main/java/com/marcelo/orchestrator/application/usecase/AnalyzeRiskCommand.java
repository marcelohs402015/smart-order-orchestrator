package com.marcelo.orchestrator.application.usecase;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Command para análise de risco.
 * 
 * <p>Contém dados necessários para iniciar análise de risco de um pedido.</p>
 * 
 * @param orderId ID do pedido a ser analisado
 * @param paymentMethod Método de pagamento utilizado (para contexto da análise)
 * @author Marcelo
 */
@Getter
@Builder
public class AnalyzeRiskCommand {
    
    private final UUID orderId;
    private final String paymentMethod;
}

