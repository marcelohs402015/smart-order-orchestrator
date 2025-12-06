package com.marcelo.orchestrator.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO de resposta da API AbacatePay para criação de cobrança.
 * 
 * <p>Representa o formato de resposta padrão do AbacatePay:
 * { "data": {...}, "error": null }</p>
 * 
 * <h3>Estrutura de Resposta AbacatePay:</h3>
 * <p>Todas as respostas seguem o padrão consistente:
 * - data: Objeto com dados da resposta (null se erro)
 * - error: Mensagem de erro (null se sucesso)</p>
 * 
 * <h3>Mapeamento para Domínio:</h3>
 * <p>Este DTO é convertido para {@code PaymentResult} do domínio
 * pelo adapter, isolando detalhes da API externa.</p>
 * 
 * @author Marcelo
 * @see <a href="https://docs.abacatepay.com">AbacatePay Documentation</a>
 */
@Getter
@Setter
public class AbacatePayBillingResponse {
    
    /**
     * Dados da cobrança (preenchido em caso de sucesso).
     */
    @JsonProperty("data")
    private AbacatePayBillingData data;
    
    /**
     * Mensagem de erro (null em caso de sucesso).
     */
    @JsonProperty("error")
    private String error;
    
    /**
     * Verifica se a resposta indica sucesso.
     * 
     * @return {@code true} se data não é null e error é null
     */
    public boolean isSuccess() {
        return data != null && error == null;
    }
    
    /**
     * Dados internos da cobrança.
     */
    @Getter
    @Setter
    public static class AbacatePayBillingData {
        
        /**
         * ID único da cobrança no AbacatePay.
         * Exemplo: "bill_12345667"
         */
        @JsonProperty("id")
        private String id;
        
        /**
         * URL para pagamento.
         * Cliente acessa esta URL para realizar pagamento.
         */
        @JsonProperty("url")
        private String url;
        
        /**
         * Valor da cobrança em centavos.
         */
        @JsonProperty("amount")
        private Integer amount;
        
        /**
         * Status da cobrança.
         * Valores possíveis: "PENDING", "PAID", "FAILED", "CANCELLED"
         */
        @JsonProperty("status")
        private String status;
        
        /**
         * Indica se está em modo de desenvolvimento.
         */
        @JsonProperty("devMode")
        private Boolean devMode;
        
        /**
         * Métodos de pagamento disponíveis.
         */
        @JsonProperty("methods")
        private String[] methods;
        
        /**
         * Frequência da cobrança.
         */
        @JsonProperty("frequency")
        private String frequency;
        
        /**
         * Dados do cliente associado.
         */
        @JsonProperty("customer")
        private AbacatePayCustomerData customer;
        
        /**
         * Data de criação.
         */
        @JsonProperty("createdAt")
        private LocalDateTime createdAt;
        
        /**
         * Data de atualização.
         */
        @JsonProperty("updatedAt")
        private LocalDateTime updatedAt;
    }
    
    /**
     * Dados do cliente na resposta.
     */
    @Getter
    @Setter
    public static class AbacatePayCustomerData {
        
        /**
         * ID do cliente no AbacatePay.
         */
        @JsonProperty("id")
        private String id;
        
        /**
         * Metadados do cliente.
         */
        @JsonProperty("metadata")
        private AbacatePayCustomerMetadata metadata;
    }
    
    /**
     * Metadados do cliente.
     */
    @Getter
    @Setter
    public static class AbacatePayCustomerMetadata {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("cellphone")
        private String cellphone;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("taxId")
        private String taxId;
    }
}

