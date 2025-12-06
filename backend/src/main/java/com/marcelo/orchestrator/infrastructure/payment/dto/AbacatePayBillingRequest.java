package com.marcelo.orchestrator.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO de requisição para criação de cobrança no AbacatePay.
 * 
 * <p>Representa o formato de dados esperado pela API do AbacatePay
 * para criar uma nova cobrança. Baseado na documentação oficial.</p>
 * 
 * <h3>Documentação AbacatePay:</h3>
 * <p>Endpoint: POST /v1/billing/create</p>
 * <p>Formato de resposta: { "data": {...}, "error": null }</p>
 * 
 * <h3>Mapeamento:</h3>
 * <p>Este DTO é criado a partir de {@code PaymentRequest} do domínio
 * e enviado para o AbacatePay. A conversão é feita pelo adapter.</p>
 * 
 * @author Marcelo
 * @see <a href="https://docs.abacatepay.com">AbacatePay Documentation</a>
 */
@Getter
@Builder
public class AbacatePayBillingRequest {
    
    /**
     * Valor da cobrança em centavos.
     * AbacatePay trabalha com valores inteiros (centavos).
     * Exemplo: R$ 10,50 = 1050 centavos
     */
    @JsonProperty("amount")
    private Integer amount; // Em centavos
    
    /**
     * Descrição da cobrança.
     * Aparece para o cliente no momento do pagamento.
     */
    @JsonProperty("description")
    private String description;
    
    /**
     * Métodos de pagamento aceitos.
     * Exemplo: ["PIX", "CARD"]
     */
    @JsonProperty("methods")
    private String[] methods;
    
    /**
     * Frequência da cobrança.
     * "ONE_TIME" para pagamento único.
     */
    @JsonProperty("frequency")
    private String frequency;
    
    /**
     * Dados do cliente (opcional - pode ser criado no momento da cobrança).
     */
    @JsonProperty("customer")
    private AbacatePayCustomerRequest customer;
    
    /**
     * Converte BigDecimal (domínio) para Integer em centavos (AbacatePay).
     * 
     * @param amount Valor em BigDecimal (ex: 10.50)
     * @return Valor em centavos (ex: 1050)
     */
    public static Integer toCents(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.multiply(BigDecimal.valueOf(100)).intValue();
    }
}

