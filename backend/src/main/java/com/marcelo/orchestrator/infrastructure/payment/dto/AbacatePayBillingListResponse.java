package com.marcelo.orchestrator.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO de resposta da API AbacatePay para listagem de cobranças.
 * 
 * <p>Representa o formato de resposta do endpoint /billing/list:
 * { "data": [...], "error": null }</p>
 * 
 * <h3>Estrutura de Resposta AbacatePay:</h3>
 * <p>O endpoint /billing/list retorna uma lista de cobranças no campo data.</p>
 * 
 * <h3>Por que Record?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Dados não podem ser alterados após criação</li>
 *   <li><strong>Simplicidade:</strong> Menos código, mais legível (Java 17+)</li>
 *   <li><strong>Performance:</strong> Menos overhead que classes tradicionais</li>
 * </ul>
 * 
 * @param data Lista de cobranças (preenchido em caso de sucesso)
 * @param error Mensagem de erro (null em caso de sucesso)
 * 
 * @author Marcelo
 * @see <a href="https://docs.abacatepay.com">AbacatePay Documentation</a>
 */
public record AbacatePayBillingListResponse(
    @JsonProperty("data")
    List<AbacatePayBillingResponse.AbacatePayBillingData> data,
    
    @JsonProperty("error")
    String error
) {
    /**
     * Verifica se a resposta indica sucesso.
     * 
     * @return {@code true} se data não é null e error é null
     */
    public boolean isSuccess() {
        return data != null && error == null;
    }
}

