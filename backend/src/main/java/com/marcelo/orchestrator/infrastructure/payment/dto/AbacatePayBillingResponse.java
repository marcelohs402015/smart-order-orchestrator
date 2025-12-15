package com.marcelo.orchestrator.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
 * <h3>Por que Record?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Dados não podem ser alterados após criação</li>
 *   <li><strong>Simplicidade:</strong> Menos código, mais legível (Java 17+)</li>
 *   <li><strong>Performance:</strong> Menos overhead que classes tradicionais</li>
 *   <li><strong>Consistência:</strong> Alinhado com padrão de DTOs do projeto (Records)</li>
 * </ul>
 * 
 * @param data Dados da cobrança (preenchido em caso de sucesso)
 * @param error Mensagem de erro (null em caso de sucesso)
 * 
 * @author Marcelo
 * @see <a href="https://docs.abacatepay.com">AbacatePay Documentation</a>
 */
public record AbacatePayBillingResponse(
    @JsonProperty("data")
    AbacatePayBillingData data,
    
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
    
    /**
     * Dados internos da cobrança.
     * 
     * <p>Record aninhado para representar a estrutura de dados
     * retornada pela API do AbacatePay.</p>
     * 
     * @param id ID único da cobrança no AbacatePay (ex: "bill_12345667")
     * @param url URL para pagamento
     * @param amount Valor da cobrança em centavos
     * @param status Status da cobrança ("PENDING", "PAID", "FAILED", "CANCELLED")
     * @param devMode Indica se está em modo de desenvolvimento
     * @param methods Métodos de pagamento disponíveis
     * @param frequency Frequência da cobrança
     * @param customer Dados do cliente associado
     * @param createdAt Data de criação
     * @param updatedAt Data de atualização
     */
    public record AbacatePayBillingData(
        @JsonProperty("id")
        String id,
        
        @JsonProperty("url")
        String url,
        
        @JsonProperty("amount")
        Integer amount,
        
        @JsonProperty("status")
        String status,
        
        @JsonProperty("devMode")
        Boolean devMode,
        
        @JsonProperty("methods")
        String[] methods,
        
        @JsonProperty("frequency")
        String frequency,
        
        @JsonProperty("customer")
        AbacatePayCustomerData customer,
        
        @JsonProperty("createdAt")
        LocalDateTime createdAt,
        
        @JsonProperty("updatedAt")
        LocalDateTime updatedAt
    ) {}
    
    /**
     * Dados do cliente na resposta.
     * 
     * @param id ID do cliente no AbacatePay
     * @param metadata Metadados do cliente
     */
    public record AbacatePayCustomerData(
        @JsonProperty("id")
        String id,
        
        @JsonProperty("metadata")
        AbacatePayCustomerMetadata metadata
    ) {}
    
    /**
     * Metadados do cliente.
     * 
     * @param name Nome do cliente
     * @param cellphone Celular do cliente
     * @param email Email do cliente
     * @param taxId CPF ou CNPJ do cliente
     */
    public record AbacatePayCustomerMetadata(
        @JsonProperty("name")
        String name,
        
        @JsonProperty("cellphone")
        String cellphone,
        
        @JsonProperty("email")
        String email,
        
        @JsonProperty("taxId")
        String taxId
    ) {}
}
